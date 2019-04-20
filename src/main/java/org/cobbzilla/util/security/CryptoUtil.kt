package org.cobbzilla.util.security

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.RandomStringUtils
import org.cobbzilla.util.string.Base64

import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.security.Key
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.string.StringUtil.UTF8

object CryptoUtil {

    val CONFIG_BLOCK_CIPHER = "AES/CBC/PKCS5Padding"
    val CONFIG_KEY_CIPHER = "AES"

    val RSA_PREFIX = "-----BEGIN RSA PRIVATE KEY-----"
    val RSA_SUFFIX = "-----END RSA PRIVATE KEY-----"

    private val MESSAGE_DIGEST: MessageDigest

    private val PADDING_SUFFIX = "__PADDING__"

    init {
        try {
            MESSAGE_DIGEST = MessageDigest.getInstance("SHA-256")
        } catch (e: NoSuchAlgorithmException) {
            throw die<Any>("error creating SHA-256 MessageDigest: $e") as RuntimeException
        }

    }

    @Throws(IOException::class)
    fun toBytes(data: InputStream): ByteArray {
        val out = ByteArrayOutputStream()
        IOUtils.copy(data, out)
        return out.toByteArray()
    }

    fun extractRsa(data: String): String? {
        val startPos = data.indexOf(RSA_PREFIX)
        if (startPos == -1) return null
        val endPos = data.indexOf(RSA_SUFFIX)
        return if (endPos == -1) null else data.substring(startPos, endPos + RSA_SUFFIX.length)
    }

    @Throws(Exception::class)
    fun encrypt(data: InputStream, passphrase: String): ByteArray {
        return encrypt(toBytes(data), passphrase)
    }

    @Throws(Exception::class)
    fun encrypt(data: ByteArray, passphrase: String): ByteArray {
        val cipher = Cipher.getInstance(CONFIG_BLOCK_CIPHER)
        val keySpec = SecretKeySpec(sha256(passphrase), CONFIG_KEY_CIPHER)
        val initVector = IvParameterSpec(ByteArray(cipher.blockSize))
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, initVector)
        return cipher.doFinal(data)
    }

    @Throws(Exception::class)
    fun sha256(passphrase: String): ByteArray {
        return ShaUtil.sha256(passphrase)
    }

    @Throws(Exception::class)
    fun decrypt(data: InputStream, passphrase: String): ByteArray {
        return decrypt(toBytes(data), passphrase)
    }

    @Throws(Exception::class)
    fun decrypt(data: ByteArray?, passphrase: String): ByteArray {
        val cipher = Cipher.getInstance(CONFIG_BLOCK_CIPHER)
        val keySpec = SecretKeySpec(sha256(passphrase), CONFIG_KEY_CIPHER)
        val initVector = IvParameterSpec(ByteArray(cipher.blockSize))
        cipher.init(Cipher.DECRYPT_MODE, keySpec, initVector)
        return cipher.doFinal(data!!)
    }

    @Throws(Exception::class)
    fun decryptStream(`in`: InputStream, passphrase: String): InputStream {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val keySpec = SecretKeySpec(sha256(passphrase), CONFIG_KEY_CIPHER)
        val initVector = IvParameterSpec(ByteArray(cipher.blockSize))
        cipher.init(Cipher.DECRYPT_MODE, keySpec, initVector)
        return CipherInputStream(`in`, cipher)
    }

    fun encryptOrDie(data: ByteArray, passphrase: String): ByteArray {
        try {
            return encrypt(data, passphrase)
        } catch (e: Exception) {
            return die("Error encrypting: $e", e)
        }

    }

    @Throws(Exception::class)
    fun pad(data: String): String {
        return data + PADDING_SUFFIX + RandomStringUtils.random(128)
    }

    fun unpad(data: String?): String? {
        if (data == null) return null
        val paddingPos = data.indexOf(PADDING_SUFFIX)
        return if (paddingPos == -1) null else data.substring(0, paddingPos)
    }

    fun string_encrypt(data: String, key: String): String {
        try {
            return Base64.encodeBytes(encryptOrDie(pad(data).toByteArray(charset(UTF8)), key))
        } catch (e: Exception) {
            return die("Error encrypting: $e", e)
        }

    }

    fun string_decrypt(data: String, key: String): String? {
        try {
            return unpad(String(decrypt(Base64.decode(data), key)))
        } catch (e: Exception) {
            return die<String>("Error decrypting: $e", e)
        }

    }

}

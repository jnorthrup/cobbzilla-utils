package org.cobbzilla.util.security

import lombok.AllArgsConstructor
import lombok.Getter
import lombok.Setter
import lombok.extern.slf4j.Slf4j
import org.apache.commons.compress.utils.IOUtils

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream

import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.daemon.ZillaRuntime.empty
import org.cobbzilla.util.security.CryptoUtil.string_decrypt
import org.cobbzilla.util.security.CryptoUtil.string_encrypt

@Slf4j
@AllArgsConstructor
class CryptoSimple : Crypto {

    @Getter
    @Setter
    override var secretKey: String? = null
        set(secretKey) {
            field = this.secretKey
        }

    fun hasSecretKey(): Boolean {
        return !empty(this.secretKey)
    }

    override fun encrypt(plaintext: String): String? {
        if (empty(this.secretKey)) die<Any>("encrypt: key was not initialized")
        return if (empty(plaintext)) null else string_encrypt(plaintext, this.secretKey)
    }

    override fun decrypt(ciphertext: String): String? {
        if (empty(this.secretKey)) die<Any>("decrypt: key was not initialized")
        return if (empty(ciphertext)) null else string_decrypt(ciphertext, this.secretKey)
    }

    // todo - support stream-oriented encryption
    override fun encrypt(`in`: InputStream, out: OutputStream) {
        if (empty(this.secretKey)) die<Any>("encrypt: key was not initialized")
        try {
            val ciphertext = CryptoUtil.encrypt(`in`, this.secretKey)
            IOUtils.copy(ByteArrayInputStream(ciphertext), out)

        } catch (e: Exception) {
            die<Any>("encryption failed: $e", e)
        }

    }

    override fun decryptBytes(`in`: InputStream): ByteArray {
        if (empty(this.secretKey)) die<Any>("encrypt: key was not initialized")
        try {
            return CryptoUtil.decrypt(`in`, this.secretKey)
        } catch (e: Exception) {
            return die("decryption failed: $e", e)
        }

    }

    override fun decryptStream(`in`: InputStream): InputStream {
        try {
            return CryptoUtil.decryptStream(`in`, this.secretKey)
        } catch (e: Exception) {
            return die("decryption failed: $e", e)
        }

    }
}

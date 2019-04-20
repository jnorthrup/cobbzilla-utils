package org.cobbzilla.util.security

import org.cobbzilla.util.string.StringUtil
import org.slf4j.Logger

import java.io.*
import java.security.DigestInputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

import org.cobbzilla.util.daemon.ZillaRuntime.die

object MD5Util {

    val HEX_DIGITS = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f")
    @JvmOverloads
    fun getMD5(bytes: ByteArray, start: Int = 0, len: Int = bytes.size): ByteArray {
        try {
            val md5 = MessageDigest.getInstance("MD5")
            md5.update(bytes, start, len)
            return md5.digest()
        } catch (e: NoSuchAlgorithmException) {
            return die("Error calculating MD5: $e")
        }

    }

    @Throws(IOException::class)
    fun md5hex(log: Logger, file: File): String {
        val BUFSIZ = 4096
        try {
            FileInputStream(file).use { fin ->
                val md5 = MessageDigest.getInstance("MD5")
                val `in` = BufferedInputStream(fin)
                val buf = ByteArray(BUFSIZ)
                var bytesRead = `in`.read(buf)
                while (bytesRead != -1) {
                    md5.update(buf, 0, bytesRead)
                    bytesRead = `in`.read(buf)
                }
                return StringUtil.tohex(md5.digest())

            }
        } catch (e: NoSuchAlgorithmException) {
            return die("Error calculating MD5: $e")
        }

    }

    fun md5hex(s: String): String {
        val bytes = getMD5(s.toByteArray())
        return StringUtil.tohex(bytes)
    }

    fun md5hex(md: MessageDigest): String {
        return StringUtil.tohex(md.digest())
    }

    @JvmOverloads
    fun md5hex(data: ByteArray, start: Int = 0, len: Int = data.size): String {
        val bytes = getMD5(data, start, len)
        return StringUtil.tohex(bytes)
    }


    fun getMD5InputStream(`in`: InputStream): MD5InputStream {
        try {
            return MD5InputStream(`in`)
        } catch (e: NoSuchAlgorithmException) {
            return die("Bad algorithm: $e")
        }

    }

    class MD5InputStream @Throws(NoSuchAlgorithmException::class)
    constructor(stream: InputStream) : DigestInputStream(stream, MessageDigest.getInstance("MD5")) {

        fun md5hex(): String {
            return MD5Util.md5hex(messageDigest)
        }
    }
}

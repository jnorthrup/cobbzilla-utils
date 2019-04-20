package org.cobbzilla.util.security

import lombok.Cleanup
import org.apache.commons.exec.CommandLine
import org.cobbzilla.util.string.Base64
import org.cobbzilla.util.string.StringUtil
import org.cobbzilla.util.system.CommandResult

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import java.net.URLEncoder
import java.security.DigestException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.io.FileUtil.abs
import org.cobbzilla.util.string.StringUtil.split
import org.cobbzilla.util.system.Bytes.MB
import org.cobbzilla.util.system.CommandShell.exec

object ShaUtil {

    val SHA256_FILE_USE_SHELL_THRESHHOLD = 10 * MB

    @Throws(NoSuchAlgorithmException::class)
    private fun md(): MessageDigest {
        return MessageDigest.getInstance("SHA-256")
    }

    fun sha256(data: String): ByteArray {
        try {
            return sha256(data.toByteArray(charset(StringUtil.UTF8)))
        } catch (e: Exception) {
            return die("sha256: bad data: $e", e)
        }

    }

    fun sha256(data: ByteArray?): ByteArray {
        if (data == null) throw NullPointerException("sha256: null argument")
        try {
            return md().digest(data)
        } catch (e: Exception) {
            return die("sha256: bad data: $e", e)
        }

    }

    fun sha256_hex(data: String): String {
        return StringUtil.tohex(sha256(data))
    }

    @Throws(Exception::class)
    fun sha256_base64(data: ByteArray): String {
        return Base64.encodeBytes(sha256(data))
    }

    @Throws(Exception::class)
    fun sha256_filename(data: String): String {
        return sha256_filename(data.toByteArray(StringUtil.UTF8cs))
    }

    fun sha256_filename(data: ByteArray): String {
        try {
            return URLEncoder.encode(Base64.encodeBytes(sha256(data)), StringUtil.UTF8)
        } catch (e: Exception) {
            return die("sha256_filename: bad byte[] data: $e", e)
        }

    }

    fun sha256_file(file: String): String {
        return sha256_file(File(file))
    }

    fun sha256_file(file: File): String {
        val result: CommandResult
        try {
            if (file.length() < SHA256_FILE_USE_SHELL_THRESHHOLD) return sha256_file_java(file)
            result = exec(CommandLine("sha256sum").addArgument(abs(file), false))
            if (result.isZeroExitStatus) return split(result.stdout, " ")[0]

        } catch (e: Exception) {
            // if we tried the shell command, it may have failed, try the pure java version
            return if (file.length() > SHA256_FILE_USE_SHELL_THRESHHOLD) sha256_file_java(file) else die("sha256sum_file: Error calculating sha256 on " + abs(file) + ": " + e)
        }

        return die("sha256sum_file: sha256sum " + abs(file) + " exited with status " + result.exitStatus + ", stderr=" + result.stderr + ", exception=" + result.exceptionString)
    }

    fun sha256_file_java(file: File): String {
        try {
            @Cleanup val input = FileInputStream(file)
            val md = getMessageDigest(input)
            return StringUtil.tohex(md.digest())
        } catch (e: Exception) {
            return die("Error calculating sha256 on " + abs(file) + ": " + e)
        }

    }

    @Throws(Exception::class)
    fun sha256_url(urlString: String): String {

        val url = URL(urlString)
        val urlConnection = url.openConnection()
        @Cleanup val input = urlConnection.getInputStream()
        val md = getMessageDigest(input)

        return StringUtil.tohex(md.digest())
    }

    @Throws(NoSuchAlgorithmException::class, IOException::class, DigestException::class)
    fun getMessageDigest(input: InputStream): MessageDigest {
        val buf = ByteArray(4096)
        val md = md()
        while (true) {
            val read = input.read(buf, 0, buf.size)
            if (read == -1) break
            md.update(buf, 0, read)
        }
        return md
    }
}

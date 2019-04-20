package org.cobbzilla.util.io

import lombok.Cleanup
import org.apache.commons.io.IOUtils
import org.cobbzilla.util.string.StringUtil
import org.slf4j.Logger

import java.io.*

import org.cobbzilla.util.daemon.ZillaRuntime.*
import org.cobbzilla.util.io.FileUtil.*

object StreamUtil {

    val SUFFIX = ".tmp"
    val PREFIX = "stream2file"
    val CLASSPATH_PROTOCOL = "classpath://"
    private val log = org.slf4j.LoggerFactory.getLogger(StreamUtil::class.java)

    val DEFAULT_BUFFER_SIZE = 32 * 1024

    fun stream2temp(path: String): File {
        return stream2file(loadResourceAsStream(path), true, path)
    }

    fun stream2temp(`in`: InputStream): File {
        return stream2file(`in`, true)
    }

    @JvmOverloads
    fun stream2file(`in`: InputStream, deleteOnExit: Boolean = false, pathOrSuffix: String = SUFFIX): File {
        try {
            return stream2file(`in`, mktemp(deleteOnExit, pathOrSuffix))
        } catch (e: IOException) {
            return die("stream2file: $e", e)
        }

    }

    @Throws(IOException::class)
    fun mktemp(deleteOnExit: Boolean, pathOrSuffix: String): File {
        val basename = if (empty(pathOrSuffix)) "" else basename(pathOrSuffix)
        val file = File.createTempFile(
                if (!basename.contains(".") || basename.length < 7) basename.replace('.', '_') + "_" + PREFIX else basename.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0],
                if (empty(pathOrSuffix)) SUFFIX else extensionOrName(pathOrSuffix),
                getDefaultTempDir())
        if (deleteOnExit) file.deleteOnExit()
        return file
    }

    @Throws(IOException::class)
    fun stream2file(`in`: InputStream, file: File): File {
        FileOutputStream(file).use { out -> IOUtils.copy(`in`, out) }
        return file
    }

    @Throws(UnsupportedEncodingException::class)
    fun toStream(s: String): ByteArrayInputStream {
        return ByteArrayInputStream(s.toByteArray(charset(StringUtil.UTF8)))
    }

    @Throws(IOException::class)
    fun toString(`in`: InputStream): String {
        val out = ByteArrayOutputStream()
        IOUtils.copy(`in`, out)
        return out.toString()
    }

    fun toStringOrDie(`in`: InputStream): String {
        try {
            return toString(`in`)
        } catch (e: Exception) {
            return die("toStringOrDie: $e", e)
        }

    }

    @JvmOverloads
    fun loadResourceAsStream(path: String, clazz: Class<*> = StreamUtil::class.java): InputStream {
        return clazz.classLoader.getResourceAsStream(path)
                ?: throw IllegalArgumentException("Resource not found: $path")
    }

    @Throws(IOException::class)
    fun loadResourceAsFile(path: String): File {
        val tmp = File.createTempFile("resource", extensionOrName(path), getDefaultTempDir())
        return loadResourceAsFile(path, StreamUtil::class.java, tmp)
    }

    @Throws(IOException::class)
    fun loadResourceAsFile(path: String, clazz: Class<*>): File {
        val tmp = File.createTempFile("resource", ".tmp", getDefaultTempDir())
        return loadResourceAsFile(path, clazz, tmp)
    }

    @Throws(IOException::class)
    fun loadResourceAsFile(path: String, file: File): File {
        return loadResourceAsFile(path, StreamUtil::class.java, file)
    }

    @Throws(IOException::class)
    fun loadResourceAsFile(path: String, clazz: Class<*>, file: File): File {
        var file = file
        if (file.isDirectory) file = File(file, File(path).name)
        @Cleanup val out = FileOutputStream(file)
        IOUtils.copy(loadResourceAsStream(path, clazz), out)
        return file
    }

    fun stream2string(path: String): String {
        return loadResourceAsStringOrDie(path)
    }

    fun stream2string(path: String, defaultValue: String): String {
        try {
            return loadResourceAsStringOrDie(path)
        } catch (e: Exception) {
            log.info("stream2string: path not found ($path: $e), returning defaultValue")
            return defaultValue
        }

    }

    fun loadResourceAsStringOrDie(path: String): String {
        try {
            return loadResourceAsString(path, StreamUtil::class.java)
        } catch (e: Exception) {
            throw IllegalArgumentException("cannot load resource: $path: $e", e)
        }

    }

    @Throws(IOException::class)
    @JvmOverloads
    fun loadResourceAsString(path: String, clazz: Class<*> = StreamUtil::class.java): String {
        @Cleanup val `in` = loadResourceAsStream(path, clazz)
        @Cleanup val out = ByteArrayOutputStream()
        IOUtils.copy(`in`, out)
        return out.toString(StringUtil.UTF8)
    }

    @Throws(IOException::class)
    fun loadResourceAsReader(resourcePath: String, clazz: Class<*>): Reader {
        return InputStreamReader(loadResourceAsStream(resourcePath, clazz))
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun copyLarge(input: InputStream, output: OutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE): Long {
        val buffer = ByteArray(bufferSize)
        var count: Long = 0
        var n = 0
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n)
            count += n.toLong()
        }
        return count
    }

    /**
     * Copy the first n bytes from input to output
     * @return the number of bytes actually copied (might be less than n if EOF was reached)
     */
    @Throws(IOException::class)
    fun copyNbytes(input: InputStream, output: OutputStream, n: Long): Long {
        val buffer = ByteArray(if (n > DEFAULT_BUFFER_SIZE) DEFAULT_BUFFER_SIZE else n.toInt())
        var copied: Long = 0
        var read = 0
        while (copied < n && -1 != (read = input.read(buffer, 0, (if (n - copied > buffer.size) buffer.size else n - copied).toInt()))) {
            output.write(buffer, 0, read)
            copied += read.toLong()
        }
        return copied
    }

    // incredibly inefficient. do not use frequently. meant for command-line tools that call it no more than a few times
    fun readLineFromStdin(): String? {
        val line: String?
        val r = stdin()
        try {
            line = r.readLine()
        } catch (e: Exception) {
            return die<String>("Error reading from stdin: $e")
        }

        return line?.trim { it <= ' ' }
    }

    fun readLineFromStdin(prompt: String): String? {
        print(prompt)
        return readLineFromStdin()
    }

    fun fromClasspathOrFilesystem(path: String): String? {
        try {
            return stream2string(path)
        } catch (e: Exception) {
            try {
                return FileUtil.toStringOrDie(path)
            } catch (e2: Exception) {
                return die<String>("path not found: $path")
            }

        }

    }

    fun fromClasspathOrString(path: String): String {
        var path = path
        val isClasspath = path.startsWith(CLASSPATH_PROTOCOL)
        if (isClasspath) {
            path = path.substring(CLASSPATH_PROTOCOL.length)
            return stream2string(path)
        }
        return path
    }

}

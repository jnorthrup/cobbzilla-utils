package org.cobbzilla.util.io

import lombok.Cleanup
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.slf4j.Logger

import java.io.*
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.TimeUnit

import org.apache.commons.lang3.StringUtils.chop
import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.daemon.ZillaRuntime.empty
import org.cobbzilla.util.io.FileUtil.EMPTY_ARRAY
import org.cobbzilla.util.io.TempDir.quickTemp

object FileUtil {

    val DEFAULT_TEMPDIR = File(System.getProperty("java.io.tmpdir"))
    private val EMPTY_ARRAY = arrayOf<File>()
    val sep = File.separator
    private val log = org.slf4j.LoggerFactory.getLogger(FileUtil::class.java)

    var defaultTempDir = System.getProperty("user.home") + "/tmp/zilla"

    // todo: ensure this works correctly in sandboxed-environments (mac app store)
    val userHomeDir: String
        get() = System.getProperty("user.home")

    val DEFAULT_KILL_AFTER = TimeUnit.MINUTES.toMillis(5)

    fun isReadableNonEmptyFile(f: File?): Boolean {
        return f != null && f.exists() && f.canRead() && f.length() > 0
    }

    /**
     * Iterate through given paths and return File object for the first found path and filename combination which
     * results with an existing readable and non empty file.
     *
     * @param paths    List of paths to look for the file in
     * @param filename The name of the file.
     * @return         First found file or null.
     */
    fun firstFoundFile(paths: Collection<String>, filename: String): File? {
        if (!empty(paths)) {
            for (path in paths) {
                val f = File(path, filename)
                if (isReadableNonEmptyFile(f)) return f
            }
        }
        // Finally try from withing the current folder:
        val f = File(filename)
        return if (isReadableNonEmptyFile(f)) f else null

    }

    fun list(dir: File): Array<File> {
        return dir.listFiles() ?: return EMPTY_ARRAY
    }

    fun listFiles(dir: File): Array<File> {
        return dir.listFiles(RegularFileFilter.instance) ?: return EMPTY_ARRAY
    }

    fun listFiles(dir: File, filter: FileFilter): Array<File> {
        return dir.listFiles(filter) ?: return EMPTY_ARRAY
    }

    fun listFilesRecursively(dir: File, filter: FileFilter): List<File> {
        val files = ArrayList<File>()
        _listRecurse(files, dir, filter)
        return files
    }

    private fun _listRecurse(results: MutableList<File>, dir: File, filter: FileFilter): List<File> {
        val files = dir.listFiles(filter) ?: return results
        results.addAll(Arrays.asList(*files))

        val subdirs = listDirs(dir)
        for (subdir in subdirs) {
            _listRecurse(results, subdir, filter)
        }
        return results
    }

    @JvmOverloads
    fun listDirs(dir: File, regex: String? = null): Array<File> {
        return dir.listFiles(if (empty(regex)) DirFilter.instance else DirFilter(regex)) ?: return EMPTY_ARRAY
    }

    fun chopSuffix(path: String?): String? {
        if (path == null) return null
        val lastDot = path.lastIndexOf('.')
        return if (lastDot == -1 || lastDot == path.length - 1) path else path.substring(0, lastDot)
    }

    @Throws(IOException::class)
    fun createTempDir(prefix: String): File {
        return createTempDir(DEFAULT_TEMPDIR, prefix)
    }

    @Throws(IOException::class)
    fun createTempDir(parentDir: File, prefix: String): File {
        val parent = FileSystems.getDefault().getPath(abs(parentDir))
        return File(Files.createTempDirectory(parent, prefix).toAbsolutePath().toString())
    }

    fun createTempDirOrDie(prefix: String): File {
        return createTempDirOrDie(DEFAULT_TEMPDIR, prefix)
    }

    fun createTempDirOrDie(parentDir: File, prefix: String): File {
        try {
            return createTempDir(parentDir, prefix)
        } catch (e: IOException) {
            return die("createTempDirOrDie: error creating directory with prefix=" + abs(parentDir) + "/" + prefix + ": " + e, e)
        }

    }

    @Throws(IOException::class)
    fun writeResourceToFile(resourcePath: String, outFile: File, clazz: Class<*>) {
        if (!outFile.parentFile.exists() || !outFile.parentFile.canWrite() || outFile.exists() && !outFile.canWrite()) {
            throw IllegalArgumentException("outFile is not writeable: " + abs(outFile))
        }
        clazz.classLoader.getResourceAsStream(resourcePath)!!.use { `in` ->
            FileOutputStream(outFile).use { out ->
                if (`in` == null) throw IllegalArgumentException("null data at resourcePath: $resourcePath")
                IOUtils.copy(`in`, out)
            }
        }
    }

    fun loadResourceAsStringListOrDie(resourcePath: String, clazz: Class<*>): List<String> {
        try {
            return loadResourceAsStringList(resourcePath, clazz)
        } catch (e: IOException) {
            throw IllegalArgumentException("loadResourceAsStringList error: $e", e)
        }

    }

    @Throws(IOException::class)
    fun loadResourceAsStringList(resourcePath: String, clazz: Class<*>): List<String> {
        @Cleanup val reader = StreamUtil.loadResourceAsReader(resourcePath, clazz)
        return toStringList(reader)
    }

    @Throws(IOException::class)
    fun toStringList(f: String): List<String> {
        return toStringList(File(f))
    }

    @Throws(IOException::class)
    fun toStringList(f: File): List<String> {
        @Cleanup val reader = FileReader(f)
        return toStringList(reader)
    }

    @Throws(IOException::class)
    fun toStringList(reader: Reader): List<String> {
        val strings = ArrayList<String>()
        BufferedReader(reader).use { r ->
            var line: String
            while ((line = r.readLine()) != null) {
                strings.add(line.trim { it <= ' ' })
            }
        }
        return strings
    }

    @Throws(IOException::class)
    fun toFile(lines: List<String>): File {
        val temp = File.createTempFile(FileUtil::class.java.simpleName + ".toFile", "tmp", getDefaultTempDir())
        BufferedWriter(FileWriter(temp)).use { writer ->
            for (line in lines) {
                writer.write(line + "\n")
            }
        }
        return temp
    }

    fun toStringOrDie(f: String): String? {
        return toStringOrDie(File(f))
    }

    fun toStringOrDie(f: File?): String? {
        try {
            return toString(f)
        } catch (e: FileNotFoundException) {
            log.warn("toStringOrDie: returning null; file not found: " + abs(f))
            return null
        } catch (e: IOException) {
            val path = f?.let { abs(it) } ?: "null"
            throw IllegalArgumentException("Error reading file ($path): $e", e)
        }

    }

    @Throws(IOException::class)
    fun toString(f: String): String? {
        return toString(File(f))
    }

    @Throws(IOException::class)
    fun toString(f: File?): String? {
        if (f == null || !f.exists()) return null
        val writer = StringWriter()
        FileReader(f).use { r -> IOUtils.copy(r, writer) }
        return writer.toString()
    }

    @Throws(IOException::class)
    fun toBytes(f: File): ByteArray {
        val out = ByteArrayOutputStream()
        FileInputStream(f).use { `in` -> IOUtils.copy(`in`, out) }
        return out.toByteArray()
    }

    fun toPropertiesOrDie(f: String): Properties {
        return toPropertiesOrDie(File(f))
    }

    private fun toPropertiesOrDie(f: File?): Properties {
        try {
            return toProperties(f)
        } catch (e: IOException) {
            val path = f?.let { abs(it) } ?: "null"
            throw IllegalArgumentException("Error reading properties file ($path): $e", e)
        }

    }

    @Throws(IOException::class)
    fun toProperties(f: String): Properties {
        return toProperties(File(f))
    }

    @Throws(IOException::class)
    fun toProperties(f: File?): Properties {
        val props = Properties()
        FileInputStream(f!!).use { `in` -> props.load(`in`) }
        return props
    }

    fun resourceToPropertiesOrDie(path: String, clazz: Class<*>): Properties {
        try {
            return resourceToProperties(path, clazz)
        } catch (e: IOException) {
            throw IllegalArgumentException("Error reading resource ($path): $e", e)
        }

    }

    @Throws(IOException::class)
    fun resourceToProperties(path: String, clazz: Class<*>): Properties {
        val props = Properties()
        StreamUtil.loadResourceAsStream(path, clazz).use { `in` -> props.load(`in`) }
        return props
    }

    fun toFileOrDie(file: String, data: String): File {
        return toFileOrDie(File(file), data)
    }

    @JvmOverloads
    fun toFileOrDie(file: File?, data: String, append: Boolean = false): File {
        try {
            return toFile(file!!, data, append)
        } catch (e: IOException) {
            val path = file?.let { abs(it) } ?: "null"
            return die("toFileOrDie: error writing to file: $path: $e", e)
        }

    }

    fun toFile(data: String): File {
        try {
            return toFile(temp(".tmp"), data, false)
        } catch (e: IOException) {
            return die("toFile: error writing data to temp file: $e", e)
        }

    }

    fun toTempFile(data: String, ext: String): File {
        return toFileOrDie(temp(ext), data, false)
    }

    @Throws(IOException::class)
    fun toFile(file: String, data: String): File {
        return toFile(File(file), data)
    }

    @Throws(IOException::class)
    fun toFile(file: File, `in`: InputStream): File {
        FileOutputStream(file).use { out -> IOUtils.copyLarge(`in`, out) }
        return file
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun toFile(file: File, data: String, append: Boolean = false): File {
        if (!ensureDirExists(file.parentFile)) {
            throw IOException("Error creating directory: " + file.parentFile)
        }
        FileOutputStream(file, append).use { out -> IOUtils.copy(ByteArrayInputStream(data.toByteArray()), out) }
        return file
    }

    fun renameOrDie(from: File, to: File) {
        if (!from.renameTo(to)) die<Any>("Error renaming " + abs(from) + " -> " + abs(to))
    }

    @Throws(IOException::class)
    fun writeString(target: File, data: String) {
        FileWriter(target).use { w -> w.write(data) }
    }

    fun writeStringOrDie(target: File, data: String) {
        try {
            writeString(target, data)
        } catch (e: IOException) {
            die<Any>("Error writing to file (" + abs(target) + "): " + e, e)
        }

    }

    fun truncate(file: File) {
        _touch(file, false)
    }

    fun touch(file: String) {
        _touch(File(file), true)
    }

    fun touch(file: File) {
        _touch(file, true)
    }

    private fun _touch(file: File?, append: Boolean) {
        try {
            FileWriter(file!!, append).use { ignored ->
                // do nothing -- if append is false, we truncate the file,
                // otherwise just update the mtime/atime, and possible create an empty file if it doesn't already exist
            }
        } catch (e: IOException) {
            val path = file?.let { abs(it) } ?: "null"
            throw IllegalArgumentException("error " + (if (append) "touching" else "truncating") + " " + path + ": " + e, e)
        }

    }

    fun path(f: File): Path {
        return FileSystems.getDefault().getPath(abs(f))
    }

    fun isSymlink(file: File): Boolean {
        return Files.isSymbolicLink(path(file))
    }

    @Throws(IOException::class)
    fun toStringExcludingLines(file: File, prefix: String): String {
        val sb = StringBuilder()
        BufferedReader(FileReader(file)).use { reader ->
            var line: String
            while ((line = reader.readLine()) != null) {
                if (!line.trim { it <= ' ' }.startsWith(prefix)) sb.append(line).append("\n")
            }
        }
        return sb.toString()
    }

    fun dirname(path: String): String {
        var path = path
        if (empty(path)) throw NullPointerException("dirname: path was empty")
        val pos = path.lastIndexOf('/')
        if (pos == -1) return "."
        if (path.endsWith("/")) path = chop(path)
        return path.substring(0, pos)
    }

    fun basename(path: String): String {
        if (empty(path)) throw NullPointerException("basename: path was empty")
        val pos = path.lastIndexOf('/')
        if (pos == -1) return path
        if (pos == path.length - 1) throw IllegalArgumentException("basename: invalid path: $path")
        return path.substring(pos + 1)
    }

    // quick alias for getting an absolute path
    fun abs(path: File?): String {
        try {
            return if (path == null) "null" else path.canonicalPath
        } catch (e: IOException) {
            log.warn("abs(" + path!!.absolutePath + "): " + e)
            return path.absolutePath
        }

    }

    fun abs(path: Path?): String {
        return if (path == null) "null" else abs(path.toFile())
    }

    fun abs(path: String?): String {
        return if (path == null) "null" else abs(File(path))
    }

    fun mkdirOrDie(dir: String): File {
        return mkdirOrDie(File(dir))
    }

    fun mkdirOrDie(dir: File): File {
        if (!dir.exists() && !dir.mkdirs()) {
            if (!dir.exists()) {
                val msg = "mkdirOrDie: error creating: " + abs(dir)
                log.error(msg)
                die<Any>(msg)
            }
        }
        assertIsDir(dir)
        return dir
    }

    fun ensureDirExists(dir: File?): Boolean {
        if (dir == null) {
            log.error("ensureDirExists: null as directory is not acceptable")
            return false
        }
        if (dir.exists() && dir.isDirectory) return true
        if (!dir.exists() && !dir.mkdirs()) {
            log.error("ensureDirExists: error creating: " + abs(dir))
            return false
        }
        if (!dir.isDirectory) {
            log.error("ensureDirExists: not a directory: " + abs(dir))
            return false
        }
        return true
    }

    fun assertIsDir(dir: File) {
        if (!dir.isDirectory) {
            val msg = "assertIsDir: not a dir: " + abs(dir)
            log.error(msg)
            throw IllegalArgumentException(msg)
        }
    }

    fun extension(f: File): String {
        return extension(abs(f))
    }

    fun extension(name: String): String {
        val lastDot = name.lastIndexOf('.')
        return if (lastDot == -1) "" else name.substring(lastDot)
    }

    fun extensionOrName(name: String): String {
        val ext = extension(name)
        return if (empty(ext)) name else ext
    }

    fun removeExtension(f: File, ext: String): String {
        return f.name.substring(0, f.name.length - ext.length)
    }

    /**
     * @param dir The directory to search
     * @return The most recently modified file, or null if the dir does not exist, is not a directory, or does not contain any files
     */
    fun mostRecentFile(dir: File): File? {
        if (!dir.exists()) return null
        var newest: File? = null
        for (file in list(dir)) {
            if (file.isDirectory) {
                file = mostRecentFile(file)
                if (file == null) continue
            }
            if (file.isFile) {
                if (newest == null) {
                    newest = file
                } else if (file.lastModified() > newest.lastModified()) {
                    newest = file
                }
            }
        }
        return newest
    }

    fun mostRecentFileIsNewerThan(dir: File, time: Long): Boolean {
        val newest = mostRecentFile(dir)
        return newest != null && newest.lastModified() > time
    }

    fun mkHomeDir(subDir: String): File {
        var subDir = subDir

        val homeDir = userHomeDir
        if (empty(homeDir)) die<Any>("mkHomeDir: System.getProperty(\"user.home\") returned nothing useful: $homeDir")

        if (!subDir.startsWith("/")) subDir = "/$subDir"

        return mkdirOrDie(File(homeDir + subDir))
    }

    fun copyFile(from: File, to: File) {
        try {
            if (!to.parentFile.exists() && !to.parentFile.mkdirs()) {
                die<Any>("Error creating parent dir: " + abs(to.parentFile))
            }
            FileUtils.copyFile(from, to)
        } catch (e: IOException) {
            die<Any>("copyFile: $e", e)
        }

    }

    fun deleteOrDie(f: File?) {
        if (f == null) return
        if (f.exists()) {
            FileUtils.deleteQuietly(f)
            if (f.exists()) die<Any>("delete: Error deleting: " + abs(f))
        }
    }

    fun countFilesWithName(dir: File, name: String): Int {
        var count = 0
        val files = dir.listFiles()
        if (files != null) {
            for (f in files) {
                if (f.isDirectory)
                    count += countFilesWithName(f, name)
                else if (f.name == name) count++
            }
        }
        return count
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun bzip2(f: File, killAfter: Long = DEFAULT_KILL_AFTER): File {
        val temp = quickTemp(killAfter)
        BZip2CompressorOutputStream(FileOutputStream(temp)).use { bzout -> FileInputStream(f).use { `in` -> IOUtils.copyLarge(`in`, bzout) } }
        return temp
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun bzip2(fileStream: InputStream, killAfter: Long = DEFAULT_KILL_AFTER): File {
        return bzip2(FileUtil.toFile(quickTemp(killAfter), fileStream))
    }

    @Throws(IOException::class)
    fun symlink(link: File, target: File): File {
        return Files.createSymbolicLink(link.toPath(), target.toPath()).toFile()
    }

    fun temp(suffix: String): File {
        return temp("temp-", suffix)
    }

    fun temp(prefix: String, suffix: String): File {
        try {
            return File.createTempFile(prefix, suffix, getDefaultTempDir())
        } catch (e: IOException) {
            return die("temp: $e", e)
        }

    }

    fun getDefaultTempDir(): File {
        return mkdirOrDie(defaultTempDir)
    }

    fun temp(prefix: String, suffix: String, dir: File): File {
        try {
            return File.createTempFile(prefix, suffix, dir)
        } catch (e: IOException) {
            return die("temp: $e", e)
        }

    }

}

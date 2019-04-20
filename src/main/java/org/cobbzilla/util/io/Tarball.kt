package org.cobbzilla.util.io

import lombok.Cleanup
import org.apache.commons.compress.archivers.ArchiveException
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.CompressorInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.exec.CommandLine
import org.apache.commons.io.FileUtils
import org.cobbzilla.util.system.Command
import org.slf4j.Logger

import java.io.*

import java.io.File.createTempFile
import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.io.FileUtil.abs
import org.cobbzilla.util.system.CommandShell.*

object Tarball {

    private val log = org.slf4j.LoggerFactory.getLogger(Tarball::class.java)

    /**
     * @param tarball the tarball to unroll. Can be .tar.gz or .tar.bz2
     * @return a File representing the temp directory where the tarball was unrolled
     */
    @Throws(Exception::class)
    fun unroll(tarball: File): TempDir {
        val tempDirectory = TempDir()
        try {
            unroll(tarball, tempDirectory)
            return tempDirectory

        } catch (e: Exception) {
            FileUtils.deleteDirectory(tempDirectory)
            throw e
        }

    }

    @Throws(IOException::class, ArchiveException::class)
    fun unroll(tarball: File, dir: File) {

        val path = abs(tarball)
        val fileIn = FileInputStream(tarball)
        val zipIn: CompressorInputStream

        if (path.toLowerCase().endsWith(".gz") || path.toLowerCase().endsWith(".tgz")) {
            zipIn = GzipCompressorInputStream(fileIn)

        } else if (path.toLowerCase().endsWith(".bz2")) {
            zipIn = BZip2CompressorInputStream(fileIn)

        } else {
            log.warn("tarball ($path) was not .tar.gz, .tgz, or .tar.bz2, assuming .tar.gz")
            zipIn = GzipCompressorInputStream(fileIn)
        }

        @Cleanup val tarIn = ArchiveStreamFactory()
                .createArchiveInputStream("tar", zipIn) as TarArchiveInputStream

        var entry: TarArchiveEntry
        while ((entry = tarIn.nextTarEntry) != null) {
            var name = entry.name
            if (name.startsWith("./")) name = name.substring(2)
            if (name.startsWith("/")) name = name.substring(1) // "root"-based files just go into current dir
            if (name.endsWith("/")) {
                val subdirName = name.substring(0, name.length - 1)
                val subdir = File(dir, subdirName)
                if (!subdir.mkdirs()) {
                    die<Any>("Error creating directory: " + abs(subdir))
                }
                continue
            }

            // when "./" gets squashed to "", we skip the entry
            if (name.trim { it <= ' ' }.length == 0) continue

            val file = File(dir, name)
            FileOutputStream(file).use { out ->
                if (StreamUtil.copyNbytes(tarIn, out, entry.size) != entry.size) {
                    die<Any>("Expected to copy " + entry.size + " bytes for " + entry.name + " in tarball " + path)
                }
            }
            chmod(file, Integer.toOctalString(entry.mode))
        }
    }

    /**
     * Roll a gzipped tarball. The tarball will be created from within the directory to be tarred (paths will be relative to .)
     * @param dir The directory to tar
     * @return The created tarball (will be a temp file)
     */
    @Throws(IOException::class)
    fun roll(dir: File): File {
        return roll(createTempFile("temp-tarball-", ".tar.gz"), dir, dir)
    }

    /**
     * Roll a gzipped tarball. The tarball will be created from "cwd", which must above the directory to be tarred.
     * @param tarball The path to the tarball to create
     * @param dir The directory to tar
     * @param cwd A directory that is somewhere above dir in the filesystem hierarchy
     * @return The created tarball
     */
    @Throws(IOException::class)
    @JvmOverloads
    fun roll(tarball: File, dir: File, cwd: File? = dir): File {
        var cwd = cwd

        if (cwd == null) cwd = dir
        val dirAbsPath = abs(dir)
        val cwdAbsPath = abs(cwd)

        val dirPath: String
        if (dirAbsPath == cwdAbsPath) {
            dirPath = "."

        } else if (dirAbsPath.startsWith(cwdAbsPath)) {
            dirPath = cwdAbsPath.substring(dirAbsPath.length)

        } else {
            return die("tarball dir is not within cwd")
        }

        val command = CommandLine("tar")
                .addArgument("czf")
                .addArgument(tarball.absolutePath)
                .addArgument(dirPath)

        okResult(exec(Command(command).setDir(cwd)))

        return tarball
    }

    fun isTarball(file: File): Boolean {
        return isTarball(file.name.toLowerCase())
    }

    fun isTarball(fileName: String): Boolean {
        return (fileName.endsWith(".tar.gz")
                || fileName.endsWith(".tar.bz2")
                || fileName.endsWith(".tgz"))
    }
}
/**
 * Roll a gzipped tarball. The tarball will be created from within the directory to be tarred (paths will be relative to .)
 * @param tarball The path to the tarball to create
 * @param dir The directory to tar
 * @return The created tarball
 */

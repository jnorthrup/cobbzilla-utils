package org.cobbzilla.util.io

import lombok.Cleanup

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.io.FileUtil.abs
import org.cobbzilla.util.io.Tarball.isTarball

// zip-related code adapted from: https://stackoverflow.com/a/10634536/1251543
object Decompressors {

    @Throws(Exception::class)
    fun unroll(infile: File): TempDir {
        if (isTarball(infile)) {
            return Tarball.unroll(infile)
        } else if (isZipFile(infile.name)) {
            val tempDir = TempDir()
            extract(infile, tempDir)
            return tempDir
        } else {
            return die("unroll: unsupported file: $infile")
        }
    }

    fun isZipFile(name: String): Boolean {
        return name.toLowerCase().endsWith(".zip")
    }

    fun isDecompressible(file: File): Boolean {
        return isDecompressible(file.name)
    }

    fun isDecompressible(name: String): Boolean {
        return isTarball(name) || isZipFile(name)
    }

    @Throws(IOException::class)
    private fun extractFile(`in`: ZipInputStream, outdir: File, name: String) {
        @Cleanup val out = FileOutputStream(File(outdir, name))
        StreamUtil.copyLarge(`in`, out)
    }

    private fun mkdirs(outdir: File, path: String) {
        val d = File(outdir, path)
        if (!d.exists() && !d.mkdirs()) die<Any>("mkdirs(" + abs(outdir) + ", " + path + "): error creating " + abs(d))
    }

    private fun dirpart(name: String): String? {
        val s = name.lastIndexOf(File.separatorChar.toInt())
        return if (s == -1) null else name.substring(0, s)
    }

    /***
     * Extract zipfile to outdir with complete directory structure
     * @param zipfile Input .zip file
     * @param outdir Output directory
     */
    @Throws(IOException::class)
    fun extract(zipfile: File, outdir: File) {
        @Cleanup val zin = ZipInputStream(FileInputStream(zipfile))
        var entry: ZipEntry
        var name: String
        var dir: String?
        while ((entry = zin.nextEntry) != null) {
            name = entry.name
            if (entry.isDirectory) {
                mkdirs(outdir, name)
                continue
            }
            /* this part is necessary because file entry can come before
             * directory entry where is file located
             * i.e.:
             *   /foo/foo.txt
             *   /foo/
             */
            dir = dirpart(name)
            if (dir != null) mkdirs(outdir, dir)

            extractFile(zin, outdir, name)
        }
    }
}

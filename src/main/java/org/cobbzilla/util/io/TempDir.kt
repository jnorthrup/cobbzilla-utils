package org.cobbzilla.util.io

import com.google.common.io.Files
import org.apache.commons.io.FileUtils
import org.slf4j.Logger

import java.io.Closeable
import java.io.File
import java.io.IOException
import java.util.SortedSet
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.TimeUnit

import org.cobbzilla.util.daemon.ZillaRuntime.*
import org.cobbzilla.util.io.FileUtil.abs
import org.cobbzilla.util.io.FileUtil.getDefaultTempDir
import org.cobbzilla.util.system.Sleep.sleep

/**
 * A directory that implements Closeable. Use lombok @Cleanup to nuke it when it goes out of scope.
 */
class TempDir : File(abs(Files.createTempDir())), Closeable {

    private val file: File

    private class FileKillOrder @java.beans.ConstructorProperties("file", "killTime")
    constructor(var file: File?, var killTime: Long) : Comparable<FileKillOrder> {

        override fun compareTo(k: FileKillOrder): Int {
            if (killTime > k.killTime) return 1
            return if (killTime == k.killTime) 0 else -1
        }

        fun shouldKill(): Boolean {
            return now() > killTime
        }
    }

    private class QuickTempReaper : Runnable {
        private val temps = ConcurrentSkipListSet<FileKillOrder>()
        @JvmOverloads
        fun add(t: File, killTime: Long = now() + TimeUnit.MINUTES.toMillis(5)): File {
            synchronized(temps) {
                temps.add(FileKillOrder(t, killTime))
                return t
            }
        }

        override fun run() {
            while (true) {
                sleep(10000)
                synchronized(temps) {
                    while (!temps.isEmpty() && temps.first().shouldKill()) {
                        if (!temps.first().file!!.delete()) {
                            log.warn("QuickTempReaper.run: couldn't delete " + abs(temps.first().file))
                        }
                        temps.remove(temps.first())
                    }
                }
            }
        }

        fun start(): QuickTempReaper {
            daemon(this)
            return this
        }
    }

    private interface TempDirOverrides {
        fun delete(): Boolean
    }

    init {
        file = File(super.getPath())
    }

    @Throws(IOException::class)
    override fun close() {
        if (!delete()) log.warn("close: error deleting TempDir: " + abs(file))
    }

    /**
     * Override to call 'delete', delete the entire directory.
     * @return true if the delete was successful.
     */
    override fun delete(): Boolean {
        return FileUtils.deleteQuietly(file)
    }

    companion object {

        private val log = org.slf4j.LoggerFactory.getLogger(TempDir::class.java)

        private val qtReaper = QuickTempReaper().start()

        val QT_NO_DELETE = -1L

        @JvmOverloads
        fun quickTemp(killAfter: Long = TimeUnit.MINUTES.toMillis(5)): File {
            try {
                if (killAfter > 0) {
                    val killTime = killAfter + now()
                    return qtReaper.add(File.createTempFile("quickTemp-", ".tmp", getDefaultTempDir()), killTime)
                } else {
                    return File.createTempFile("quickTemp-", ".tmp", getDefaultTempDir())
                }
            } catch (e: IOException) {
                return die("quickTemp: cannot create temp file: $e", e)
            }

        }
    }

}

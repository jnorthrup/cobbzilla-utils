package org.cobbzilla.util.io

import lombok.experimental.Accessors
import org.cobbzilla.util.daemon.AwaitResult
import org.cobbzilla.util.string.StringUtil
import org.slf4j.Logger

import java.io.File
import java.io.FileFilter
import java.util.ArrayList
import java.util.Arrays
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

import org.cobbzilla.util.daemon.Await.awaitAll
import org.cobbzilla.util.daemon.DaemonThreadFactory.fixedPool
import org.cobbzilla.util.io.FileUtil.abs
import org.cobbzilla.util.io.FileUtil.isSymlink
import org.cobbzilla.util.system.Sleep.sleep
import org.cobbzilla.util.time.TimeUtil.parseDuration

@Accessors(chain = true)
class FilesystemWalker {
    private val dirs = ArrayList<File>()
    private val visitors = ArrayList<FilesystemVisitor>()
    private var includeSymlinks = true
    private var visitDirs = false
    private var threads = 5
    private var size = 1000000
    private var timeout = TimeUnit.MINUTES.toMillis(15)
    private var filter: FileFilter? = null
    private var sleepTime = TimeUnit.SECONDS.toMillis(5)

    val pool = fixedPool(getThreads())
    private val futures = ArrayList<Future<*>>(getSize())

    fun hasFilter(): Boolean {
        return filter != null
    }

    fun withDir(dir: File): FilesystemWalker {
        dirs.add(dir)
        return this
    }

    fun withDirs(dirs: List<File>): FilesystemWalker {
        this.dirs.addAll(dirs)
        return this
    }

    fun withDirs(dirs: Array<File>): FilesystemWalker {
        this.dirs.addAll(Arrays.asList(*dirs))
        return this
    }

    fun withVisitor(visitor: FilesystemVisitor): FilesystemWalker {
        visitors.add(visitor)
        return this
    }

    fun withTimeoutDuration(duration: String): FilesystemWalker {
        setTimeout(parseDuration(duration))
        return this
    }

    fun walk(): AwaitResult<*> {
        for (dir in dirs) fileJob(dir)

        // wait for number of futures to stop increasing
        do {
            val lastNumFutures = numFutures()
            awaitFutures()
            if (numFutures() == lastNumFutures) break
            sleep(getSleepTime())
        } while (true)
        return awaitFutures()
    }

    private fun awaitFutures(): AwaitResult<*> {
        val result = awaitAll<Any>(getFutures(), getTimeout())
        if (!result.allSucceeded()) log.warn(StringUtil.toString(result.failures.values, "\n---------"))
        return result
    }

    private fun numFutures(): Int {
        val futures = getFutures()

        synchronized(futures) {
            return futures.size
        }
    }

    private fun fileJob(f: File): Boolean {
        val futures = getFutures()
        val future = pool.submit(FsWalker(f))

        synchronized(futures) {
            return futures.add(future)
        }
    }

    fun getDirs(): List<File> {
        return this.dirs
    }

    fun getVisitors(): List<FilesystemVisitor> {
        return this.visitors
    }

    fun isIncludeSymlinks(): Boolean {
        return this.includeSymlinks
    }

    fun isVisitDirs(): Boolean {
        return this.visitDirs
    }

    fun getThreads(): Int {
        return this.threads
    }

    fun getSize(): Int {
        return this.size
    }

    fun getTimeout(): Long {
        return this.timeout
    }

    fun getFilter(): FileFilter? {
        return this.filter
    }

    fun getSleepTime(): Long {
        return this.sleepTime
    }

    fun getFutures(): MutableList<Future<*>> {
        return this.futures
    }

    fun setIncludeSymlinks(includeSymlinks: Boolean): FilesystemWalker {
        this.includeSymlinks = includeSymlinks
        return this
    }

    fun setVisitDirs(visitDirs: Boolean): FilesystemWalker {
        this.visitDirs = visitDirs
        return this
    }

    fun setThreads(threads: Int): FilesystemWalker {
        this.threads = threads
        return this
    }

    fun setSize(size: Int): FilesystemWalker {
        this.size = size
        return this
    }

    fun setTimeout(timeout: Long): FilesystemWalker {
        this.timeout = timeout
        return this
    }

    fun setFilter(filter: FileFilter): FilesystemWalker {
        this.filter = filter
        return this
    }

    fun setSleepTime(sleepTime: Long): FilesystemWalker {
        this.sleepTime = sleepTime
        return this
    }

    private inner class FsWalker @java.beans.ConstructorProperties("file")
    constructor(private val file: File) : Runnable {

        override fun run() {
            if (isSymlink(file) && !includeSymlinks) return

            if (file.isFile) {
                // visit the file
                visit()

            } else if (file.isDirectory) {
                // should we visit directory entries?
                if (visitDirs) visit()

                // should we filter directory entries?
                val files = if (hasFilter()) file.listFiles(filter) else file.listFiles()

                // walk each entry in the directory
                if (files != null) for (f in files) fileJob(f)

            } else {
                log.warn("unexpected file: neither file nor directory, skipping: " + abs(file))
            }

        }

        private fun visit() {
            for (visitor in getVisitors()) visitor.visit(file)
        }
    }

    companion object {

        private val log = org.slf4j.LoggerFactory.getLogger(FilesystemWalker::class.java)
    }
}

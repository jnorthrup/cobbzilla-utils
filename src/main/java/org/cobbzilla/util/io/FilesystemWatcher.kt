package org.cobbzilla.util.io

import lombok.Cleanup
import org.cobbzilla.util.system.Sleep
import org.slf4j.Logger

import java.io.Closeable
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.daemon.ZillaRuntime.terminate
import org.cobbzilla.util.io.FileUtil.abs

open class FilesystemWatcher : Runnable, Closeable {

    private val thread = Thread(this)
    private val done = AtomicBoolean(false)
    val path: Path

    /**
     * If the path does not exist, we cannot create the watch. But we can keep trying, and we do.
     * @return how long to wait before retrying to create the watch, if the path didn't exist
     */
    protected val sleepWhileNotExists: Long
        get() = 10000

    /**
     * If null is returned, the watcher will terminate on any unexpected Exception
     * @return how long to sleep after some other unknown Exception (besides InterruptedException) occurs.
     */
    protected val sleepAfterUnexpectedError: Int?
        get() = 10000

    constructor(path: File) {
        this.path = path.toPath()
    }

    constructor(path: Path) {
        this.path = path
    }

    @Synchronized
    fun start() {
        done.set(false)
        thread.isDaemon = true
        thread.start()
    }

    @Synchronized
    fun stop() {
        done.set(true)
        terminate(thread, STOP_TIMEOUT)
    }

    @Throws(IOException::class)
    override fun close() {
        stop()
    }

    // print the events and the affected file
    protected open fun handleEvent(event: WatchEvent<*>) {

        val kind = event.kind()
        val path = if (event.context() is Path) event.context() as Path else null
        val file = path?.toFile()

        if (file == null) {
            log.warn("null path in event: $event")
            return
        }

        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
            if (file.isDirectory) {
                onDirCreated(toFile(path))
            } else {
                onFileCreated(toFile(path))
            }
        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
            if (file.isDirectory) {
                onDirDeleted(toFile(path))
            } else {
                onFileDeleted(toFile(path))
            }
        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
            if (file.isDirectory) {
                onDirModified(toFile(path))
            } else {
                onFileModified(toFile(path))
            }
        }
    }

    protected fun onDirCreated(path: File) {
        log.info("dir created: " + abs(path))
    }

    protected fun onFileCreated(path: File) {
        log.info("file created: " + abs(path))
    }

    protected fun onDirModified(path: File) {
        log.info("dir modified: " + abs(path))
    }

    protected fun onFileModified(path: File) {
        log.info("file modified: " + abs(path))
    }

    protected fun onDirDeleted(path: File) {
        log.info("dir deleted: " + abs(path))
    }

    protected fun onFileDeleted(path: File) {
        log.info("file deleted: " + abs(path))
    }

    fun toFile(p: Path): File {
        return File(path.toFile(), p.toFile().name)
    }

    override fun run() {
        var logNotExists = true
        while (!done.get()) {
            try {
                log.info("Registering watch service on $path")
                @Cleanup val watchService = path.fileSystem.newWatchService()
                path.register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE)
                logNotExists = true

                // loop forever to watch directory
                while (!done.get()) {
                    val watchKey: WatchKey
                    watchKey = watchService.take() // this call is blocking until events are present

                    // poll for file system events on the WatchKey
                    log.info("Waiting for FS events on $path")
                    for (event in watchKey.pollEvents()) {
                        log.info("Handling event: " + event.kind().name() + " " + event.context())
                        handleEvent(event)
                    }

                    // if the watched directed gets deleted, get out of run method
                    if (!watchKey.reset()) {
                        log.warn("watchKey could not be reset, perhaps path ($path) was removed?")
                        watchKey.cancel()
                        watchService.close()
                        break
                    }
                }

            } catch (e: InterruptedException) {
                die<Any>("watch thread interrupted, exiting: $e", e)

            } catch (e: NoSuchFileException) {
                if (logNotExists) {
                    log.warn("watch dir does not exist, waiting for it to exist: $e")
                    logNotExists = false
                }
                Sleep.sleep(sleepWhileNotExists, "waiting for path to exist: " + abs(path))

            } catch (e: Exception) {
                if (sleepAfterUnexpectedError != null) {
                    log.warn("error in watch thread, waiting to re-create the watch: $e", e)
                    Sleep.sleep(sleepAfterUnexpectedError!!.toLong())

                } else {
                    die<Any>("error in watch thread, exiting: $e", e)
                }
            }

        }
    }

    override fun toString(): java.lang.String {
        return "FilesystemWatcher(done=" + this.done + ", path=" + this.path + ")"
    }

    companion object {

        val STOP_TIMEOUT = TimeUnit.SECONDS.toMillis(5)
        private val log = org.slf4j.LoggerFactory.getLogger(FilesystemWatcher::class.java)
    }
}

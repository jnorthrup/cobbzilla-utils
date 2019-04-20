package org.cobbzilla.util.io

import org.cobbzilla.util.collection.InspectCollection
import org.cobbzilla.util.system.Sleep

import java.io.Closeable
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.nio.file.WatchEvent
import java.util.ArrayList
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

import org.cobbzilla.util.daemon.ZillaRuntime.now
import org.cobbzilla.util.daemon.ZillaRuntime.terminate
import org.cobbzilla.util.io.FileUtil.abs

/**
 * Sometimes you just want to know that something changed, and you don't really care what.
 * Extend this class and override the "fire" method. You will receive one callback when
 * your timeout elapses, or if the buffer of events exceeds maxEvents.
 */
abstract class BufferedFilesystemWatcher(path: Path, val timeout: Long, val maxEvents: Int) : FilesystemWatcher(path), Closeable {

    private val monitor: Thread?
    private var lastFlush: Long = 0
    private val buffer = ConcurrentLinkedQueue<WatchEvent<*>>()
    private val bfsMonitor: BfsMonitor

    /**
     * Called when some changes have occurred.
     * This will be called if the number of events exceeds maxEvents, or if
     * timeout milliseconds have elapsed since the last time it was called (any
     * at least one event has occurred)
     * @param events A collection of events.
     */
    protected abstract fun fire(events: List<WatchEvent<*>>)

    init {
        bfsMonitor = BfsMonitor()
        monitor = Thread(bfsMonitor, "bfs-monitor(" + abs(path) + ")")
        monitor!!.isDaemon = true
        monitor.start()
    }

    constructor(path: File, timeout: Long, maxEvents: Int) : this(path.toPath(), timeout, maxEvents) {}

    @Throws(IOException::class)
    override fun close() {
        if (monitor != null) {
            bfsMonitor.alive = false
            terminate(monitor, 2000)
        }
        super.close()
    }

    private fun beenTooLong(): Boolean {
        return now() - lastFlush > timeout
    }

    private fun bufferTooBig(): Boolean {
        return InspectCollection.isLargerThan(buffer, maxEvents)
    }

    private fun shouldFlush(): Boolean {
        return bufferTooBig() || !buffer.isEmpty() && beenTooLong()
    }

    override fun handleEvent(event: WatchEvent<*>) {
        buffer.add(event)
    }

    override fun toString(): java.lang.String {
        return "BufferedFilesystemWatcher(super=" + super.toString() + ", timeout=" + this.timeout + ", maxEvents=" + this.maxEvents + ")"
    }

    private inner class BfsMonitor : Runnable {
        @Volatile
        var alive = false

        override fun run() {
            alive = true
            while (alive) {
                Sleep.sleep(timeout / 10)
                if (shouldFlush()) flush()
            }
        }
    }

    @Synchronized
    private fun flush() {
        // sanity check that we have not flushed recently
        if (!shouldFlush()) return

        // nothing to flush?
        if (buffer.isEmpty()) return

        val events = ArrayList<WatchEvent<*>>(buffer.size)
        while (!buffer.isEmpty()) {
            events.add(buffer.poll())
            if (events.size > maxEvents) {
                fire(events)
                events.clear()
            }
        }
        if (!events.isEmpty()) fire(events)
        lastFlush = now()
    }

}

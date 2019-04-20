package org.cobbzilla.util.io

import org.slf4j.Logger

import java.io.File
import java.nio.file.Path
import java.nio.file.WatchEvent
import java.util.ArrayList
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

import org.cobbzilla.util.system.Sleep.nap

abstract class DamperedCompositeBufferedFilesystemWatcher : CompositeBufferedFilesystemWatcher {
    private val damper = AtomicLong(0)
    private val damperThread = AtomicReference<Thread>()
    private val buffer = AtomicReference<List<WatchEvent<*>>>()

    protected fun init(damperDuration: Long, maxEvents: Int) {

        this.damper.set(damperDuration)
        this.buffer.set(ArrayList(maxEvents * 10))

        this.damperThread.set(Thread(Runnable {
            log.debug(status() + " starting, sleeping for a while until there is some activity")

            while (true) {
                if (!nap(TimeUnit.HOURS.toMillis(4), "waiting for filesystem watcher trigger to fire")) {
                    // we were interrupted. sleep for the damper time
                    while (!nap(damper.get())) {
                        // there was more activity, go back to sleep
                        log.debug(status() + " more activity while napping for damper, trying again")
                    }
                    // we successfully napped without being interrupted! fire the big trigger
                    log.debug(status() + ": napped successfully, calling fire")
                    val events: List<WatchEvent<*>>
                    synchronized(buffer) {
                        events = ArrayList(buffer.get())
                        buffer.get().clear()
                    }
                    uber_fire(events)
                }
                log.debug(status() + " just fired, going back to sleep for a while until there is some more activity")
            }
        }))
        damperThread.get().isDaemon = true
        damperThread.get().start()
    }

    protected fun status(): String {
        synchronized(buffer) {
            return "[" + buffer.get().size + " events]"
        }
    }

    /**
     * Called when the thing finally really fires.
     * @param events
     */
    abstract fun uber_fire(events: List<WatchEvent<*>>)

    override fun fire(events: List<WatchEvent<*>>) {
        log.debug(status() + ": fire adding " + events.size + " events...")
        synchronized(buffer) {
            buffer.get().addAll(events)
        }
        synchronized(damperThread.get()) {
            damperThread.get().interrupt()
        }
    }

    constructor(timeout: Long, maxEvents: Int, damper: Long) : super(timeout, maxEvents) {
        init(damper, maxEvents)
    }

    constructor(timeout: Long, maxEvents: Int, paths: Array<File>, damper: Long) : super(timeout, maxEvents, paths) {
        init(damper, maxEvents)
    }

    constructor(timeout: Long, maxEvents: Int, paths: Array<String>, damper: Long) : super(timeout, maxEvents, paths) {
        init(damper, maxEvents)
    }

    constructor(timeout: Long, maxEvents: Int, paths: Array<Path>, damper: Long) : super(timeout, maxEvents, paths) {
        init(damper, maxEvents)
    }

    constructor(timeout: Long, maxEvents: Int, things: Collection<*>, damper: Long) : super(timeout, maxEvents, things) {
        init(damper, maxEvents)
    }

    companion object {

        private val log = org.slf4j.LoggerFactory.getLogger(DamperedCompositeBufferedFilesystemWatcher::class.java)
    }

}

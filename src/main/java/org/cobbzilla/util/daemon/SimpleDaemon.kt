package org.cobbzilla.util.daemon

import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.slf4j.Logger

import org.cobbzilla.util.daemon.ZillaRuntime.now
import org.cobbzilla.util.daemon.ZillaRuntime.readStdin
import org.cobbzilla.util.system.Sleep.sleep

abstract class SimpleDaemon : Runnable {

    var name: String? = null
        private set
    var lastProcessTime: Long = 0
        private set

    @Volatile
    private var mainThread: Thread? = null
    private val lock = Any()
    @Volatile
    var isDone = false
        private set

    protected val startupDelay: Long
        get() = 0

    protected abstract val sleepTime: Long

    val isAlive: Boolean
        get() {
            try {
                return mainThread != null && mainThread!!.isAlive
            } catch (npe: NullPointerException) {
                return false
            }

        }

    val status: String
        get() = ("isDone=" + isDone
                + "\nlastProcessTime=" + DFORMAT.print(lastProcessTime)
                + "\nsleepTime=" + sleepTime + "ms")

    constructor() {
        this.name = javaClass.simpleName
    }

    constructor(name: String) {
        this.name = name
    }

    /** Called right after daemon has started  */
    fun onStart() {}

    /** Called right before daemon is about to exit  */
    fun onStop() {}

    fun start() {
        log.info(name!! + ": Starting daemon")
        synchronized(lock) {
            if (mainThread != null) {
                log.warn(name!! + ": daemon is already running, not starting it again")
                return
            }
            mainThread = Thread(this)
        }
        mainThread!!.isDaemon = true
        mainThread!!.start()
    }

    private fun alreadyStopped(): Boolean {
        if (mainThread == null) {
            log.warn(name!! + ": daemon is already stopped")
            return true
        }
        return false
    }

    fun stop() {
        if (alreadyStopped()) return
        isDone = true
        mainThread!!.interrupt()
        // Let's leave it at that, this thread is a daemon anyway.
    }

    fun interrupt() {
        if (alreadyStopped()) return
        mainThread!!.interrupt()
    }


    @Deprecated("USE WITH CAUTION -- calls Thread.stop() !!")
    private fun kill() {
        if (alreadyStopped()) return
        isDone = true
        mainThread!!.stop()
    }

    /**
     * Tries to stop the daemon.  If it doesn't stop within "wait" millis,
     * it gets killed.
     */
    fun stopWithPossibleKill(wait: Long) {
        stop()
        val start = now()
        while (isAlive && now() - start < wait) {
            wait(25, "stopWithPossibleKill")
        }
        if (isAlive) {
            kill()
        }
    }

    @Throws(Exception::class)
    protected fun init() {
    }

    override fun run() {
        onStart()
        val delay = startupDelay
        if (delay > 0) {
            log.debug(name + ": Delaying daemon startup for " + delay + "ms...")
            if (!wait(delay, "run[startup-delay]")) return
        }
        log.debug(name!! + ": Daemon thread now running")

        try {
            log.debug(name!! + ": Daemon thread invoking init")
            init()

            while (!isDone) {
                log.debug(name!! + ": Daemon thread invoking process")
                process()
                lastProcessTime = now()
                if (isDone) return
                if (!wait(sleepTime, "run[post-processing]")) return
            }
        } catch (e: Exception) {
            log.error("$name: Error in daemon, exiting: $e", e)

        } finally {
            cleanup()
            try {
                onStop()
            } catch (e: Exception) {
                log.error("$name: Error in onStop, exiting and ignoring error: $e", e)
            }

        }
    }

    protected fun wait(delay: Long, reason: String): Boolean {
        try {
            sleep(delay, reason)
            return true
        } catch (e: RuntimeException) {
            if (isDone) {
                log.info("sleep(" + readStdin() + ") interrupted but daemon is done")
            } else {
                log.error("sleep(" + readStdin() + ") interrupted, exiting: " + e)
            }
            return false
        }

    }

    protected abstract fun process()

    private fun cleanup() {
        mainThread = null
        isDone = true
    }

    companion object {

        val DFORMAT = DateTimeFormat.forPattern("yyyy-MMM-dd HH:mm:ss")
        private val log = org.slf4j.LoggerFactory.getLogger(SimpleDaemon::class.java)
    }
}

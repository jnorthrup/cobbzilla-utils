package org.cobbzilla.util.daemon

import org.slf4j.Logger

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

import java.util.concurrent.TimeUnit.HOURS
import java.util.concurrent.TimeUnit.SECONDS
import org.cobbzilla.util.daemon.ZillaRuntime.background
import org.cobbzilla.util.daemon.ZillaRuntime.now
import org.cobbzilla.util.system.Sleep.nap
import org.cobbzilla.util.time.TimeUtil.formatDuration

class BufferedRunDaemon @java.beans.ConstructorProperties("logPrefix", "action")
constructor(private val logPrefix: String, private val action: Runnable) : Runnable {

    private val daemonThread = AtomicReference<Thread>()
    private val lastRun = AtomicLong(0)
    private val lastRunRequested = AtomicLong(0)

    private val done = AtomicBoolean(false)

    protected val idleSyncInterval: Long
        get() = IDLE_SYNC_INTERVAL
    protected val minSyncWait: Long
        get() = MIN_SYNC_WAIT

    fun start() {
        daemonThread.set(background(this))
    }

    protected fun interrupt() {
        if (daemonThread.get() != null) daemonThread.get().interrupt()
    }

    fun poke() {
        lastRunRequested.set(now())
        interrupt()
    }

    fun done() {
        done.set(true)
        interrupt()
    }

    override fun run() {
        var napTime: Long


        while (true) {
            napTime = idleSyncInterval
            log.info(logPrefix + ": sleep for " + formatDuration(napTime) + " awaiting activity")
            if (!nap(napTime, logPrefix + " napping for " + formatDuration(napTime) + " awaiting activity")) {
                log.info("$logPrefix interrupted during initial pause, continuing")
            } else {
                var shouldDoIdleSleep = lastRunRequested.get() == 0L
                if (shouldDoIdleSleep) {
                    shouldDoIdleSleep = lastRunRequested.get() == 0L
                    while (shouldDoIdleSleep && lastRun.get() > 0 && now() - lastRun.get() < idleSyncInterval) {
                        log.info(logPrefix + " napping for " + formatDuration(napTime) + " due to no activity")
                        if (!nap(napTime, "$logPrefix idle loop sleep")) {
                            log.info("$logPrefix nap was interrupted, breaking out")
                            break
                        }
                        shouldDoIdleSleep = lastRunRequested.get() == 0L
                    }
                }
            }

            val minSyncWait = minSyncWait
            while (lastRunRequested.get() > 0 && now() - lastRunRequested.get() < minSyncWait) {
                napTime = minSyncWait / 4
                log.info(logPrefix + " napping for " + formatDuration(napTime) + ", waiting for at least " + formatDuration(minSyncWait) + " of no activity before starting sync")
                nap(napTime, "$logPrefix waiting for inactivity")
            }

            try {
                action.run()
            } catch (e: Exception) {
                log.error("$logPrefix sync: $e", e)
            } finally {
                lastRun.set(now())
                lastRunRequested.set(0)
            }
        }
    }

    companion object {

        val IDLE_SYNC_INTERVAL = HOURS.toMillis(1)
        val MIN_SYNC_WAIT = SECONDS.toMillis(10)
        private val log = org.slf4j.LoggerFactory.getLogger(BufferedRunDaemon::class.java)
    }
}

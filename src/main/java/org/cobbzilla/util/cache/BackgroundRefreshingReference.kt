package org.cobbzilla.util.cache

import org.cobbzilla.util.system.Sleep
import org.slf4j.Logger

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

abstract class BackgroundRefreshingReference<T> : AutoRefreshingReference<T>() {
    private val updateInProgress = AtomicBoolean(false)
    private val refresher = Refresher()
    private val errorCount = AtomicInteger(0)

    fun initialize(): Boolean {
        return true
    }

    init {
        if (initialize()) update()
    }

    override fun update() {
        synchronized(updateInProgress) {
            if (updateInProgress.get()) return
            updateInProgress.set(true)
            Thread(refresher).start()
        }
    }

    private inner class Refresher : Runnable {
        override fun run() {
            try {
                val errCount = errorCount.get()
                if (errCount > 0) {
                    Sleep.sleep(TimeUnit.SECONDS.toMillis(1) * Math.pow(2.0, Math.min(errCount, 6).toDouble()).toLong())
                }
                set(refresh())
                errorCount.set(0)

            } catch (e: Exception) {
                log.warn("error refreshing: $e")
                errorCount.incrementAndGet()

            } finally {
                synchronized(updateInProgress) {
                    updateInProgress.set(false)
                }
            }
        }
    }

    companion object {

        private val log = org.slf4j.LoggerFactory.getLogger(BackgroundRefreshingReference<*>::class.java)
    }
}

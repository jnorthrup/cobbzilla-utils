package org.cobbzilla.util.daemon

import org.slf4j.Logger

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

class DaemonThreadFactory : ThreadFactory {

    override fun newThread(r: Runnable): Thread {
        val t = Thread(r)
        t.isDaemon = true
        return t
    }

    companion object {

        val instance = DaemonThreadFactory()
        private val log = org.slf4j.LoggerFactory.getLogger(DaemonThreadFactory::class.java)

        fun fixedPool(count: Int): ExecutorService {
            var count = count
            if (count <= 0) {
                log.warn("fixedPool: invalid count ($count), using single thread")
                count = 1
            }
            return Executors.newFixedThreadPool(count, instance)
        }
    }

}

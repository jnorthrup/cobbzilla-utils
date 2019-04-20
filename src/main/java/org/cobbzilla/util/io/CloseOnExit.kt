package org.cobbzilla.util.io

import org.slf4j.Logger

import java.io.Closeable
import java.util.ArrayList

class CloseOnExit private constructor() : Runnable {

    override fun run() {
        if (closeables != null) {
            for (c in closeables) {
                try {
                    c.close()
                } catch (e: Exception) {
                    log.error("Error closing: $c: $e", e)
                }

            }
        }
    }

    companion object {

        private val log = org.slf4j.LoggerFactory.getLogger(CloseOnExit::class.java)
        private val closeables = ArrayList<Closeable>()

        init {
            Runtime.getRuntime().addShutdownHook(Thread(CloseOnExit()))
        }

        fun add(closeable: Closeable) {
            closeables.add(closeable)
        }
    }

}

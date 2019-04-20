package org.cobbzilla.util.time

import org.cobbzilla.util.daemon.ZillaRuntime

interface ClockProvider {

    fun now(): Long

    companion object {

        val SYSTEM: ClockProvider = object : ClockProvider {
            override fun now(): Long {
                return System.currentTimeMillis()
            }
        }
        val ZILLA: ClockProvider = object : ClockProvider {
            override fun now(): Long {
                return ZillaRuntime.now()
            }
        }
    }

}

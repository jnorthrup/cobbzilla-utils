package org.cobbzilla.util.network

import java.io.IOException
import java.net.ServerSocket

import org.cobbzilla.util.daemon.ZillaRuntime.die

object PortPicker {

    @Throws(IOException::class)
    fun pick(): Int {
        ServerSocket(0).use { s -> return s.localPort }
    }

    fun pickOrDie(): Int {
        try {
            return pick()
        } catch (e: IOException) {
            return die("Error picking port: $e", e)
        }

    }

}
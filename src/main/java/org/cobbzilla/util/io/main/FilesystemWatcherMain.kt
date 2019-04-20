package org.cobbzilla.util.io.main

import org.apache.commons.exec.CommandLine
import org.apache.commons.lang3.exception.ExceptionUtils
import org.cobbzilla.util.io.DamperedCompositeBufferedFilesystemWatcher
import org.cobbzilla.util.main.BaseMain
import org.cobbzilla.util.system.CommandShell
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.slf4j.Logger

import java.nio.file.WatchEvent

import org.cobbzilla.util.daemon.ZillaRuntime.now

class FilesystemWatcherMain : BaseMain<FilesystemWatcherMainOptions>() {

    @Throws(Exception::class)
    override fun run() {

        val options = options

        val watcher = object : DamperedCompositeBufferedFilesystemWatcher(options!!.timeout.toLong(), options.maxEvents, options.watchPaths, options.damperMillis) {
            override fun uber_fire(events: List<WatchEvent<*>>) {
                try {
                    if (options!!.hasCommand()) {
                        CommandShell.exec(CommandLine(options.command!!))
                    } else {
                        val msg = status() + " uber_fire (" + events.size + " events) at " + DFORMAT.print(now())
                        log.info(msg)
                        println(msg)
                    }
                } catch (e: Exception) {
                    val msg = (status() + " Error running command (" + options!!.command + "): "
                            + e + "\n" + ExceptionUtils.getStackTrace(e))
                    log.error(msg, e)
                    System.err.println(msg)
                }

            }
        }

        synchronized(watcher) {
            watcher.wait()
        }
    }

    companion object {

        val DFORMAT = DateTimeFormat.forPattern("yyyy-MMM-dd HH:mm:ss")
        private val log = org.slf4j.LoggerFactory.getLogger(FilesystemWatcherMain::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            BaseMain.main(FilesystemWatcherMain::class.java, args)
        }
    }

}

package org.cobbzilla.util.io.main

import org.cobbzilla.util.collection.SingletonSet
import org.cobbzilla.util.main.BaseMainOptions
import org.kohsuke.args4j.Argument
import org.kohsuke.args4j.Option

import org.cobbzilla.util.daemon.ZillaRuntime.empty

class FilesystemWatcherMainOptions : BaseMainOptions() {
    @Option(name = OPT_COMMAND, aliases = [LONGOPT_COMMAND], usage = USAGE_COMMAND, required = false)
    var command: String? = null
    @Option(name = OPT_TIMEOUT, aliases = [LONGOPT_TIMEOUT], usage = USAGE_TIMEOUT, required = false)
    var timeout = DEFAULT_TIMEOUT
    @Option(name = OPT_MAXEVENTS, aliases = [LONGOPT_MAXEVENTS], usage = USAGE_MAXEVENTS, required = false)
    var maxEvents = DEFAULT_MAXEVENTS
    @Option(name = OPT_DAMPER, aliases = [LONGOPT_DAMPER], usage = USAGE_DAMPER, required = false)
    var damper = 0
    val damperMillis: Long
        get() = (damper * 1000).toLong()
    @Argument(usage = USAGE_PATHS)
    var paths: List<String>? = null
    val watchPaths: Collection<*>?
        get() = if (hasPaths()) paths else SingletonSet(System.getProperty("user.dir"))

    fun hasCommand(): Boolean {
        return !empty(command)
    }

    fun hasPaths(): Boolean {
        return !empty(paths)
    }

    companion object {

        val USAGE_COMMAND = "Command to run when something changes, must be executable. Default is to print the changes detected."
        val OPT_COMMAND = "-c"
        val LONGOPT_COMMAND = "--command"

        val DEFAULT_TIMEOUT = 600
        val USAGE_TIMEOUT = "Command will be run after this timeout (in seconds), regardless of any changes. Default is " + DEFAULT_TIMEOUT + " seconds (" + DEFAULT_TIMEOUT / 60 + " minutes)."
        val OPT_TIMEOUT = "-t"
        val LONGOPT_TIMEOUT = "--timeout"

        val DEFAULT_MAXEVENTS = 100
        val USAGE_MAXEVENTS = "Command will be run after this many events have occurred. Default is $DEFAULT_MAXEVENTS"
        val OPT_MAXEVENTS = "-m"
        val LONGOPT_MAXEVENTS = "--max-events"

        val USAGE_DAMPER = "Command will never be run until there have been no events for this many seconds. Default is 0 (disabled). Takes precedence over $OPT_TIMEOUT/$LONGOPT_TIMEOUT"
        val OPT_DAMPER = "-d"
        val LONGOPT_DAMPER = "--damper"

        val USAGE_PATHS = "Paths to watch for changes. Default is the current directory"
    }
}

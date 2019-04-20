package org.cobbzilla.util.io.main

import org.cobbzilla.util.main.BaseMainOptions
import org.kohsuke.args4j.Option

import java.io.File

class UniqueFileWalkerOptions : BaseMainOptions() {
    @Option(name = OPT_DIR, aliases = [LONGOPT_DIR], usage = USAGE_DIR, required = true)
    var dirs: Array<File>? = null
    @Option(name = OPT_TIMEOUT, aliases = [LONGOPT_TIMEOUT], usage = USAGE_TIMEOUT)
    var timeoutDuration = "1d"
    @Option(name = OPT_SIZE, aliases = [LONGOPT_SIZE], usage = USAGE_SIZE)
    var size = 1000000
    @Option(name = OPT_THREADS, aliases = [LONGOPT_THREADS], usage = USAGE_THREADS)
    var threads = 5
    @Option(name = OPT_OUTFILE, aliases = [LONGOPT_OUTFILE], usage = USAGE_OUTFILE)
    var outfile: File? = null

    fun hasOutfile(): Boolean {
        return outfile != null
    }

    companion object {

        val USAGE_DIR = "Add a directory to the search"
        val OPT_DIR = "-d"
        val LONGOPT_DIR = "--dir"

        val USAGE_TIMEOUT = "Timeout duration. For example 10m for ten minutes. use h for hours, d for days."
        val OPT_TIMEOUT = "-t"
        val LONGOPT_TIMEOUT = "--timeout"

        val USAGE_SIZE = "Rough guess to number of files to visit."
        val OPT_SIZE = "-s"
        val LONGOPT_SIZE = "--size"

        val USAGE_THREADS = "Degree of parallelism"
        val OPT_THREADS = "-p"
        val LONGOPT_THREADS = "--parallel"

        val USAGE_OUTFILE = "Output file"
        val OPT_OUTFILE = "-o"
        val LONGOPT_OUTFILE = "--outfile"
    }
}

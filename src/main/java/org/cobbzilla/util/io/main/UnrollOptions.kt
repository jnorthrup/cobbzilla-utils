package org.cobbzilla.util.io.main

import org.cobbzilla.util.main.BaseMainOptions
import org.kohsuke.args4j.Option

import java.io.File

class UnrollOptions : BaseMainOptions() {
    @Option(name = OPT_FILE, aliases = [LONGOPT_FILE], usage = USAGE_FILE, required = true)
    var file: File? = null

    companion object {

        val USAGE_FILE = "File to unroll"
        val OPT_FILE = "-f"
        val LONGOPT_FILE = "--file"
    }
}

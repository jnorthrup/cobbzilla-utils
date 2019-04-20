package org.cobbzilla.util.json.main

import org.cobbzilla.util.json.JsonEditOperationType
import org.cobbzilla.util.main.BaseMainOptions
import org.kohsuke.args4j.Option

import java.io.File

import org.cobbzilla.util.io.StreamUtil.toStringOrDie

class JsonEditorOptions : BaseMainOptions() {
    @Option(name = OPT_CONFIG_FILE, aliases = [LONGOPT_CONFIG_FILE], usage = USAGE_CONFIG_FILE)
    var jsonFile: File? = null

    val inputJson: String
        get() = toStringOrDie(BaseMainOptions.inStream(jsonFile))
    @Option(name = OPT_OPERATION, aliases = [LONGOPT_OPERATION], usage = USAGE_OPERATION)
    var operationType = JsonEditOperationType.read
    @Option(name = OPT_PATH, aliases = [LONGOPT_PATH], usage = USAGE_PATH)
    var path: String? = null
    @Option(name = OPT_VALUE, aliases = [LONGOPT_VALUE], usage = USAGE_VALUE)
    var value: String? = null
    @Option(name = OPT_OUTPUT, aliases = [LONGOPT_OUTPUT], usage = USAGE_OUTPUT)
    var outfile: File? = null

    fun hasOutfile(): Boolean {
        return outfile != null
    }

    companion object {

        val USAGE_CONFIG_FILE = "The JSON file to source. Default is standard input."
        val OPT_CONFIG_FILE = "-f"
        val LONGOPT_CONFIG_FILE = "--file"

        val USAGE_OPERATION = "The operation to perform."
        val OPT_OPERATION = "-o"
        val LONGOPT_OPERATION = "--operation"

        val USAGE_PATH = "The path to the JSON node where the append, replace or sort will take place. " +
                "Default is root node for append or sort operations. For replace, you must specify a path. " +
                "For sort operations, path must be an array."
        val OPT_PATH = "-p"
        val LONGOPT_PATH = "--path"

        val USAGE_VALUE = "The JSON data to append or update, or the field path to sort on. Required for write and sort operations."
        val OPT_VALUE = "-v"
        val LONGOPT_VALUE = "--value"

        val USAGE_OUTPUT = "The output file. Default is standard output."
        val OPT_OUTPUT = "-w"
        val LONGOPT_OUTPUT = "--outfile"
    }
}

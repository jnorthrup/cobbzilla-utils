package org.cobbzilla.util.main

import org.kohsuke.args4j.Option

import java.io.*
import java.lang.reflect.Field

import org.cobbzilla.util.daemon.ZillaRuntime.*

open class BaseMainOptions {
    @Option(name = OPT_HELP, aliases = [LONGOPT_HELP], usage = USAGE_HELP)
    var isHelp: Boolean = false
    @Option(name = OPT_VERBOSE_FATAL_ERRORS, aliases = [LONGOPT_VERBOSE_FATAL_ERRORS], usage = USAGE_VERBOSE_FATAL_ERRORS)
    var isVerboseFatalErrors = false

    fun out(s: String) {
        println(s)
    }

    fun err(s: String) {
        System.err.println(s)
    }

    fun required(field: String) {
        try {
            val optField = javaClass.getField("OPT_$field")
            val longOptField = javaClass.getField("LONGOPT_$field")
            err("Missing option: " + optField.get(null) + "/" + longOptField.get(null))
        } catch (e: Exception) {
            die<Any>("No such field: $field: $e", e)
        }

    }

    fun requiredAndDie(field: String) {
        try {
            val optField = javaClass.getField("OPT_$field")
            val longOptField = javaClass.getField("LONGOPT_$field")
            die<Any>("Missing option: " + optField.get(null) + "/" + longOptField.get(null))
        } catch (e: Exception) {
            die<Any>("No such field: $field: $e", e)
        }

    }

    companion object {

        val USAGE_HELP = "Show help for this command"
        val OPT_HELP = "-h"
        val LONGOPT_HELP = "--help"

        val USAGE_VERBOSE_FATAL_ERRORS = "Verbose fatal errors"
        val OPT_VERBOSE_FATAL_ERRORS = "-z"
        val LONGOPT_VERBOSE_FATAL_ERRORS = "--verbose-fatal-errors"

        fun inStream(file: File?): InputStream {
            try {
                return file?.let { FileInputStream(it) } ?: System.`in`
            } catch (e: Exception) {
                return die("inStream: $e", e)
            }

        }

        fun outStream(file: File?): OutputStream {
            try {
                return file?.let { FileOutputStream(it) } ?: System.out
            } catch (e: Exception) {
                return die("outStream: $e", e)
            }

        }

        fun reader(file: File?): BufferedReader {
            try {
                return if (file != null) BufferedReader(FileReader(file)) else stdin()
            } catch (e: Exception) {
                return die("reader: $e", e)
            }

        }

        fun writer(file: File?): BufferedWriter {
            try {
                return BufferedWriter(file?.let { FileWriter(it) } ?: stdout())
            } catch (e: Exception) {
                return die("writer: $e", e)
            }

        }
    }
}

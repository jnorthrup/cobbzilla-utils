package org.cobbzilla.util.handlebars.main

import lombok.Getter
import lombok.Setter
import org.cobbzilla.util.json.JsonUtil
import org.cobbzilla.util.main.BaseMainOptions
import org.kohsuke.args4j.Option

import java.io.File
import java.io.InputStream
import java.util.HashMap

class PdfMergeOptions : BaseMainOptions() {
    @Option(name = OPT_INFILE, aliases = [LONGOPT_INFILE], usage = USAGE_INFILE)
    @Getter
    @Setter
    var infile: File? = null
        set(infile) {
            field = this.infile
        }

    val inputStream: InputStream
        get() = BaseMainOptions.inStream(infile)
    @Option(name = OPT_CTXFILE, aliases = [LONGOPT_CTXFILE], usage = USAGE_CTXFILE)
    @Getter
    @Setter
    var contextFile: File? = null
        set(contextFile) {
            field = this.contextFile
        }

    val context: Map<String, Any>?
        @Throws(Exception::class)
        get() = if (this.contextFile == null) HashMap() else JsonUtil.fromJson<Map<*, *>>(this.contextFile, Map<*, *>::class.java)
    @Option(name = OPT_OUTFILE, aliases = [LONGOPT_OUTFILE], usage = USAGE_OUTFILE)
    @Getter
    @Setter
    var outfile: File? = null
        set(outfile) {
            field = this.outfile
        }

    fun hasOutfile(): Boolean {
        return this.outfile != null
    }

    companion object {

        val USAGE_INFILE = "Input file. Default is stdin"
        val OPT_INFILE = "-i"
        val LONGOPT_INFILE = "--infile"

        val USAGE_CTXFILE = "Context file, must be a JSON map of String->Object"
        val OPT_CTXFILE = "-c"
        val LONGOPT_CTXFILE = "--context"

        val USAGE_OUTFILE = "Output file. Default is a random temp file"
        val OPT_OUTFILE = "-o"
        val LONGOPT_OUTFILE = "--outfile"
    }

}

package org.cobbzilla.util.handlebars.main

import lombok.Getter
import lombok.Setter
import org.cobbzilla.util.main.BaseMainOptions
import org.kohsuke.args4j.Argument
import org.kohsuke.args4j.Option

import java.io.File
import java.io.OutputStream

class PdfConcatOptions : BaseMainOptions() {
    @Option(name = OPT_OUTFILE, aliases = [LONGOPT_OUTFILE], usage = USAGE_OUTFILE)
    @Getter
    @Setter
    var outfile: File? = null
        set(outfile) {
            field = this.outfile
        }

    val out: OutputStream
        get() = BaseMainOptions.outStream(this.outfile)
    @Argument(usage = USAGE_INFILES)
    @Getter
    @Setter
    var infiles: List<String>? = null
        set(infiles) {
            field = this.infiles
        }
    @Option(name = OPT_MAX_MEMORY, aliases = [LONGOPT_MAX_MEMORY], usage = USAGE_MAX_MEMORY)
    @Getter
    @Setter
    var maxMemory: Long = -1
        set(maxMemory) {
            field = this.maxMemory
        }
    @Option(name = OPT_MAX_DISK, aliases = [LONGOPT_MAX_DISK], usage = USAGE_MAX_DISK)
    @Getter
    @Setter
    var maxDisk: Long = -1
        set(maxDisk) {
            field = this.maxDisk
        }

    companion object {

        val USAGE_OUTFILE = "Output file. Default is stdout."
        val OPT_OUTFILE = "-o"
        val LONGOPT_OUTFILE = "--output"

        val USAGE_INFILES = "Show help for this command"

        val USAGE_MAX_MEMORY = "Max memory to use. Default is unlimited"
        val OPT_MAX_MEMORY = "-m"
        val LONGOPT_MAX_MEMORY = "--max-memory"

        val USAGE_MAX_DISK = "Max disk to use. Default is unlimited"
        val OPT_MAX_DISK = "-d"
        val LONGOPT_MAX_DISK = "--max-disk"
    }

}

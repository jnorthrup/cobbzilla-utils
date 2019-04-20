package org.cobbzilla.util.handlebars.main

import lombok.Cleanup
import org.cobbzilla.util.handlebars.PdfMerger
import org.cobbzilla.util.main.BaseMain

import java.io.OutputStream

class PdfConcatMain : BaseMain<PdfConcatOptions>() {

    @Throws(Exception::class)
    override fun run() {
        val options = options
        @Cleanup val out = options!!.out
        PdfMerger.concatenate(options.infiles!!, out, options.maxMemory, options.maxDisk)
        BaseMain.out("success")
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            BaseMain.main(PdfConcatMain::class.java, args)
        }
    }

}

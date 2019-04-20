package org.cobbzilla.util.handlebars.main

import com.github.jknack.handlebars.Handlebars
import lombok.Cleanup
import lombok.Getter
import org.cobbzilla.util.error.GeneralErrorHandler
import org.cobbzilla.util.handlebars.PdfMerger
import org.cobbzilla.util.main.BaseMain
import org.cobbzilla.util.string.StringUtil

import java.io.File
import java.io.InputStream
import java.util.ArrayList

import org.cobbzilla.util.daemon.ZillaRuntime.empty
import org.cobbzilla.util.io.FileUtil.abs

class PdfMergeMain : BaseMain<PdfMergeOptions>() {

    @Getter
    var handlebars: Handlebars? = null
        protected set

    @Throws(Exception::class)
    override fun run() {
        val options = options

        val errors = ArrayList<String>()
        PdfMerger.setErrorHandler(object : GeneralErrorHandler {
            override fun <T> handleError(message: String): T? {
                errors.add(message)
                return null
            }

            override fun <T> handleError(message: String, e: Exception): T? {
                return handleError<T>(message + ": " + e.javaClass.simpleName + ": " + e.message)
            }

            override fun <T> handleError(validationErrors: List<String>): T? {
                errors.addAll(validationErrors)
                return null
            }
        })
        @Cleanup val `in` = options!!.inputStream
        try {
            if (options.hasOutfile()) {
                val outfile = options.outfile
                PdfMerger.merge(`in`, outfile, options.context, handlebars)
                BaseMain.out(abs(outfile))

            } else {
                val output = PdfMerger.merge(`in`, options.context, handlebars)
                BaseMain.out(abs(output))
            }
        } catch (e: Exception) {
            BaseMain.err("Unexpected exception merging PDF: " + e.javaClass.simpleName + ": " + e.message)
        }

        if (!empty(errors)) {
            BaseMain.err(errors.size.toString() + " error" + (if (errors.size > 1) "s" else "") + " found when merging PDF:\n" + StringUtil.toString(errors, "\n"))
        }
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            BaseMain.main(PdfMergeMain::class.java, args)
        }
    }

}

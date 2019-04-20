package org.cobbzilla.util.handlebars

import com.github.jknack.handlebars.Handlebars
import fr.opensagres.poi.xwpf.converter.xhtml.XHTMLConverter
import fr.opensagres.poi.xwpf.converter.xhtml.XHTMLOptions
import lombok.extern.slf4j.Slf4j
//import org.apache.poi.xwpf.converter.xhtml.XHTMLConverter;
//import org.apache.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.cobbzilla.util.http.HtmlScreenCapture
import org.cobbzilla.util.io.FileUtil
import org.cobbzilla.util.xml.TidyHandlebarsSpanMerger

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

import org.cobbzilla.util.io.FileUtil.temp
import org.cobbzilla.util.xml.TidyUtil.tidy

@Slf4j
object WordDocxMerger {

    @Throws(Exception::class)
    fun merge(`in`: InputStream,
              context: Map<String, Any>,
              capture: HtmlScreenCapture,
              handlebars: Handlebars): File {

        // convert to HTML
        val document = XWPFDocument(`in`)
        val mergedHtml = temp(".html")
        FileOutputStream(mergedHtml).use { out ->
            val options = XHTMLOptions.create().setIgnoreStylesIfUnused(true)
            XHTMLConverter.getInstance().convert(document, out, options)
        }

        // - tidy HTML file
        // - merge consecutive <span> tags (which might occur in the middle of a {{variable}})
        // - replace HTML-entities encoded within handlebars templates (for example, convert &lsquo; and &rsquo; to single-quote char)
        // - apply Handlebars
        var tidyHtml = tidy(mergedHtml, TidyHandlebarsSpanMerger.instance)
        tidyHtml = TidyHandlebarsSpanMerger.scrubHandlebars(tidyHtml)
        FileUtil.toFile(mergedHtml, HandlebarsUtil.apply(handlebars, tidyHtml, context))

        // convert HTML -> PDF
        val pdfOutput = temp(".pdf")
        capture.capture(mergedHtml, pdfOutput)

        return pdfOutput
    }

}

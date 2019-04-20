package org.cobbzilla.util.handlebars

import com.github.jknack.handlebars.Handlebars
import lombok.Cleanup
import lombok.Getter
import lombok.extern.slf4j.Slf4j
import org.apache.commons.io.FileUtils
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.pdmodel.*
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox
import org.apache.pdfbox.pdmodel.interactive.form.PDField
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField
import org.cobbzilla.util.error.GeneralErrorHandler

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicReference

import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.daemon.ZillaRuntime.empty
import org.cobbzilla.util.error.GeneralErrorHandlerBase.defaultErrorHandler
import org.cobbzilla.util.io.FileUtil.abs
import org.cobbzilla.util.io.FileUtil.temp
import org.cobbzilla.util.reflect.ReflectionUtil.instantiate

@Slf4j
object PdfMerger {

    @Getter
    val errorHandler = defaultErrorHandler()

    val NULL_FORM_VALUE = "þÿ"
    val CTX_IMAGE_INSERTIONS = "imageInsertions"
    val CTX_TEXT_INSERTIONS = "textInsertions"
    fun setErrorHandler(handler: GeneralErrorHandler) {
        errorHandler.set(handler)
    }

    @Throws(Exception::class)
    fun merge(`in`: InputStream,
              outfile: File,
              context: Map<String, Any>,
              handlebars: Handlebars) {
        val out = merge(`in`, context, handlebars)
        if (empty(out)) die<Any>("merge: no outfiles generated")
        if (!out!!.renameTo(outfile)) die<Any>("merge: error renaming " + abs(out) + "->" + abs(outfile))
    }

    @Throws(Exception::class)
    fun merge(`in`: InputStream,
              context: Map<String, Any>,
              handlebars: Handlebars): File? {
        return merge(`in`, context, handlebars, ArrayList())
    }

    @Throws(Exception::class)
    fun merge(`in`: InputStream,
              context: Map<String, Any>,
              handlebars: Handlebars,
              validationErrors: MutableList<String>?): File? {

        val fieldMappings = context["fields"] as Map<String, String>

        // load the document
        @Cleanup val pdfDocument = PDDocument.load(`in`)

        // get the document catalog
        val acroForm = pdfDocument.documentCatalog.acroForm

        // as there might not be an AcroForm entry a null check is necessary
        if (acroForm != null) {
            acroForm.setNeedAppearances(false)

            // Retrieve an individual field and set its value.
            for (field in acroForm.fields) {
                try {
                    var fieldValue: String? = if (fieldMappings == null) null else fieldMappings[field.fullyQualifiedName]
                    if (!empty(fieldValue)) {
                        fieldValue = safeApply(context, handlebars, fieldValue, validationErrors)
                        if (fieldValue == null) continue
                    }
                    if (field is PDCheckBox) {
                        if (!empty(fieldValue)) {
                            if (java.lang.Boolean.valueOf(fieldValue)) {
                                field.check()
                            } else {
                                field.unCheck()
                            }
                        }

                    } else {
                        var formValue: String? = field.valueAsString
                        if (formValue == NULL_FORM_VALUE) formValue = null
                        if (empty(formValue) && field is PDTextField) {
                            formValue = field.defaultValue
                            if (formValue == NULL_FORM_VALUE) formValue = null
                        }
                        if (empty(formValue)) formValue = fieldValue
                        if (!empty(formValue)) {
                            formValue = safeApply(context, handlebars, formValue, validationErrors)
                            if (formValue == null) continue
                            try {
                                field.setValue(formValue)
                            } catch (e: Exception) {
                                errorHandler.get().handleError<Any>("merge (field=$field, value=$formValue): $e", e)
                            }

                        }
                    }
                } catch (e: Exception) {
                    errorHandler.get().handleError<Any>("merge: $e", e)
                }

                field.isReadOnly = true
                field.cosObject.setInt("Ff", 1)
            }
            // acroForm.flatten();
            acroForm.setNeedAppearances(false)
        }

        // add images
        val imageInsertions = context[CTX_IMAGE_INSERTIONS] as Map<String, Any>
        if (!empty(imageInsertions)) {
            for (insertion in imageInsertions.values) {
                insertImage(pdfDocument, insertion, Base64ImageInsertion::class.java)
            }
        }

        // add text
        val textInsertions = context[CTX_TEXT_INSERTIONS] as Map<String, Any>
        if (!empty(textInsertions)) {
            for (insertion in textInsertions.values) {
                insertImage(pdfDocument, insertion, TextImageInsertion::class.java)
            }
        }

        val output = temp(".pdf")

        // Save and close the filled out form.
        pdfDocument.documentCatalog.pageMode = PageMode.USE_THUMBS
        pdfDocument.save(output)

        if (validationErrors != null && !validationErrors.isEmpty()) {
            errorHandler.get().handleError<Any>(validationErrors)
            return null
        }
        return output
    }

    fun safeApply(context: Map<String, Any>, handlebars: Handlebars, fieldValue: String, validationErrors: MutableList<String>?): String? {
        try {
            return HandlebarsUtil.apply(handlebars, fieldValue, context)
        } catch (e: Exception) {
            if (validationErrors != null) {
                log.warn("safeApply($fieldValue): $e")
                validationErrors.add(fieldValue + "\t" + e.message)
                return null
            } else {
                throw e
            }
        }

    }

    @Throws(IOException::class)
    internal fun insertImage(pdfDocument: PDDocument, insert: Any, clazz: Class<out ImageInsertion>) {
        val insertion: ImageInsertion
        if (insert is ImageInsertion) {
            insertion = insert
        } else if (insert is Map<*, *>) {
            insertion = instantiate<out ImageInsertion>(clazz)
            insertion.init(insert as Map<String, Any>)
        } else {
            die<Any>("insertImage(" + clazz.simpleName + "): invalid object: " + insert)
            return
        }

        // write image to temp file
        var imageTemp: File? = null
        try {
            imageTemp = insertion.imageFile
            if (imageTemp != null) {
                // create PD image
                val image = PDImageXObject.createFromFile(abs(imageTemp), pdfDocument)
                val pages = pdfDocument.documentCatalog.pages
                val insertionHeight = insertion.height
                if (insertion.isWatermark) {
                    for (page in pages) {
                        // set x, y, width and height to center insertion and maximize size on page
                        val y = page.bBox.height / 2.0f - insertionHeight
                        insertion.setX(20f)
                                .setY(y)
                                .setWidth(page.bBox.width - 20).height = page.bBox.height - 10
                        insertImageOnPage(image, insertion, pdfDocument, page)
                    }
                } else {
                    insertImageOnPage(image, insertion, pdfDocument, pages.get(insertion.page))
                }
            }
        } finally {
            if (imageTemp != null && !imageTemp.delete()) log.warn("insertImage(" + clazz.simpleName + "): error deleting image file: " + abs(imageTemp))
        }
    }

    @Throws(IOException::class)
    private fun insertImageOnPage(image: PDImageXObject, insertion: ImageInsertion, pdfDocument: PDDocument, page: PDPage) {
        // open stream for writing inserted image
        val contentStream = PDPageContentStream(pdfDocument, page, PDPageContentStream.AppendMode.APPEND, true)

        // draw image on page
        contentStream.drawImage(image, insertion.x, insertion.y, insertion.width, insertion.height)
        contentStream.close()
    }

    @Throws(IOException::class)
    fun concatenate(infiles: List<*>, out: OutputStream, maxMemory: Long, maxDisk: Long) {
        val merger = PDFMergerUtility()
        for (infile in infiles) {
            if (infile is File) {
                merger.addSource(infile)
            } else if (infile is InputStream) {
                merger.addSource(infile)
            } else if (infile is String) {
                merger.addSource(infile)
            } else {
                die<Any>("concatenate: invalid infile (" + infile.javaClass.getName() + "): " + infile)
            }
        }
        merger.destinationStream = out
        merger.mergeDocuments(MemoryUsageSetting.setupMixed(maxMemory, maxDisk))
    }

    @Throws(IOException::class)
    fun scrubAcroForm(file: File, output: OutputStream) {
        @Cleanup val pdfIn = FileUtils.openInputStream(file)
        @Cleanup val pdfDoc = PDDocument.load(pdfIn)
        val acroForm = pdfDoc.documentCatalog.acroForm

        if (acroForm == null) {
            Files.copy(file.toPath(), output)
        } else {
            acroForm.needAppearances = false

            val tempFile = temp(".pdf")
            pdfDoc.save(tempFile)
            pdfDoc.close()
            Files.copy(tempFile.toPath(), output)
            tempFile.delete()
        }
    }

}

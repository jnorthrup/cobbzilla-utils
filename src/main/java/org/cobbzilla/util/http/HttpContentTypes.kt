package org.cobbzilla.util.http

import org.cobbzilla.util.collection.MapBuilder
import org.cobbzilla.util.collection.NameAndValue

import org.apache.commons.lang3.StringEscapeUtils.*
import org.apache.http.HttpHeaders.CONTENT_TYPE
import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.daemon.ZillaRuntime.empty

object HttpContentTypes {

    val TEXT_HTML = "text/html"
    val TEXT_PLAIN = "text/plain"
    val TEXT_CSV = "text/csv"
    val APPLICATION_JSON = "application/json"
    val APPLICATION_XML = "application/xml"
    val APPLICATION_PDF = "application/pdf"
    val IMAGE_PNG = "image/png"
    val IMAGE_JPEG = "image/jpg"
    val IMAGE_GIF = "image/gif"
    val APPLICATION_OCTET_STREAM = "application/octet-stream"
    val UNKNOWN = APPLICATION_OCTET_STREAM
    val APPLICATION_ZIP = "application/zip"
    val APPLICATION_JAR = "application/java-archive"
    val APPLICATION_GZIP = "application/gzip"

    val NV_HTTP_JSON = nvHttp(APPLICATION_JSON)
    val NV_HTTP_XML = nvHttp(APPLICATION_XML)

    val HTTP_CONTENT_TYPES = MapBuilder.build<String, Array<NameAndValue>>(arrayOf(arrayOf<Any>(APPLICATION_JSON, NV_HTTP_JSON), arrayOf<Any>(APPLICATION_XML, NV_HTTP_XML)))
    // useful when constructing HttpRequestBeans that will be used against a JSON API

    private fun nvHttp(type: String): Array<NameAndValue> {
        return arrayOf(NameAndValue(CONTENT_TYPE, type))
    }

    fun contentType(name: String): String {
        val dot = name.lastIndexOf('.')
        val ext = if (dot != -1 && dot != name.length - 1) name.substring(dot + 1) else name
        when (ext) {
            "htm", "html" -> return TEXT_HTML
            "png" -> return IMAGE_PNG
            "jpg", "jpeg" -> return IMAGE_JPEG
            "gif" -> return IMAGE_GIF
            "xml" -> return APPLICATION_XML
            "pdf" -> return APPLICATION_PDF
            "json" -> return APPLICATION_JSON
            "gz", "tgz" -> return APPLICATION_GZIP
            "zip" -> return APPLICATION_ZIP
            "jar" -> return APPLICATION_JAR
            "txt" -> return TEXT_PLAIN
            "csv" -> return TEXT_CSV
            else -> return die("contentType: no content-type could be determined for name: $name")
        }
    }

    fun fileExt(contentType: String): String {
        when (contentType) {
            TEXT_HTML -> return ".html"
            TEXT_PLAIN -> return ".txt"
            TEXT_CSV -> return ".csv"
            IMAGE_PNG -> return ".png"
            IMAGE_JPEG -> return ".jpeg"
            IMAGE_GIF -> return ".gif"
            APPLICATION_XML -> return ".xml"
            APPLICATION_PDF -> return ".pdf"
            APPLICATION_JSON -> return ".json"
            APPLICATION_ZIP -> return ".zip"
            APPLICATION_GZIP -> return ".tar.gz"
            else -> return die("fileExt: no file extension could be determined for content-type: $contentType")
        }
    }

    fun fileExtNoDot(contentType: String): String {
        return fileExt(contentType).substring(1)
    }

    fun escape(mime: String, data: String): String? {
        when (mime) {
            APPLICATION_XML -> return escapeXml10(data)
            TEXT_HTML -> return escapeHtml4(data)
        }
        return data
    }

    fun unescape(mime: String, data: String): String? {
        if (empty(data)) return data
        when (mime) {
            APPLICATION_XML -> return unescapeXml(data)
            TEXT_HTML -> return unescapeHtml4(data)
        }
        return data
    }

    fun multipartWithBoundary(boundary: String): String {
        return "multipart/form-data; boundary=$boundary"
    }

}

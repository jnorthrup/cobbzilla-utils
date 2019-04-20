package org.cobbzilla.util.http

import com.fasterxml.jackson.annotation.JsonIgnore
import lombok.experimental.Accessors
import org.apache.commons.io.IOUtils
import org.apache.http.Header
import org.apache.http.HttpHeaders
import org.cobbzilla.util.collection.NameAndValue
import org.cobbzilla.util.json.JsonUtil
import org.slf4j.Logger

import java.io.IOException
import java.io.InputStream
import java.util.*

import org.apache.http.HttpHeaders.CONTENT_LENGTH
import org.apache.http.HttpHeaders.CONTENT_TYPE
import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.daemon.ZillaRuntime.empty
import org.cobbzilla.util.string.StringUtil.UTF8cs

@Accessors(chain = true)
class HttpResponseBean {

    private var status: Int = 0
    private var headers: MutableList<NameAndValue>? = null
    @JsonIgnore
    var entity: ByteArray? = null
        private set
    private var contentLength: Long = 0
    private var contentType: String? = null

    val isOk: Boolean
        @JsonIgnore get() = status / 100 == 2

    val entityString: String?
        get() {
            try {
                return if (entity == null) null else String(entity!!, UTF8cs)
            } catch (e: Exception) {
                log.warn("getEntityString: error parsing bytes: $e")
                return null
            }

        }

    fun toMap(): Map<String, Any> {
        val map = LinkedHashMap<String, Any>()
        map["status"] = status
        if (!empty(headers)) map["headers"] = headers!!.toTypedArray()
        map["entity"] = if (hasContentType()) HttpContentTypes.escape(contentType()!!, entityString) else entityString
        return map
    }

    fun hasHeader(name: String): Boolean {
        return !empty(getHeaderValues(name))
    }

    fun hasContentType(): Boolean {
        return contentType != null || hasHeader(HttpHeaders.CONTENT_TYPE)
    }

    fun contentType(): String? {
        return if (contentType != null) contentType else getFirstHeaderValue(HttpHeaders.CONTENT_TYPE)
    }

    fun addHeader(name: String, value: String) {
        if (headers == null) headers = ArrayList()
        if (name.equals(CONTENT_TYPE, ignoreCase = true))
            setContentType(value)
        else if (name.equals(CONTENT_LENGTH, ignoreCase = true)) setContentLength(java.lang.Long.valueOf(value))
        headers!!.add(NameAndValue(name, value))
    }

    fun setEntityBytes(bytes: ByteArray): HttpResponseBean {
        this.entity = bytes
        return this
    }

    fun setEntity(entity: InputStream?): HttpResponseBean {
        try {
            this.entity = if (entity == null) null else IOUtils.toByteArray(entity)
            return this
        } catch (e: IOException) {
            return die("setEntity: error reading stream: $e", e)
        }

    }

    fun hasEntity(): Boolean {
        return !empty(entity)
    }

    fun <T> getEntity(clazz: Class<T>): T? {
        return if (entity == null) null else JsonUtil.fromJsonOrDie(entityString, clazz)
    }

    fun getHeaderValues(name: String): Collection<String> {
        val values = ArrayList<String>()
        if (!empty(headers)) for (header in headers!!) if (header.name!!.equals(name, ignoreCase = true)) values.add(header.value)
        return values
    }


    fun getFirstHeaderValue(name: String): String? {
        if (empty(headers)) return null
        for (header in headers!!) if (header.name!!.equals(name, ignoreCase = true)) return header.value
        return null
    }

    fun setHttpHeaders(headers: Array<Header>): HttpResponseBean {
        for (header in headers) {
            addHeader(header.name, header.value)
        }
        return this
    }

    fun setHttpHeaders(h: Map<String, List<String>>): HttpResponseBean {
        if (empty(h)) return this
        for ((key, value) in h) {
            if (!empty(key)) {
                for (v in value) {
                    if (!empty(v)) addHeader(key, v)
                }
            }
        }
        return this
    }

    override fun toString(): java.lang.String {
        return "HttpResponseBean(status=" + this.status + ", headers=" + this.headers + ")"
    }

    fun getStatus(): Int {
        return this.status
    }

    fun getHeaders(): List<NameAndValue>? {
        return this.headers
    }

    fun getContentLength(): Long {
        return this.contentLength
    }

    fun getContentType(): String? {
        return this.contentType
    }

    fun setStatus(status: Int): HttpResponseBean {
        this.status = status
        return this
    }

    fun setHeaders(headers: MutableList<NameAndValue>): HttpResponseBean {
        this.headers = headers
        return this
    }

    fun setContentLength(contentLength: Long): HttpResponseBean {
        this.contentLength = contentLength
        return this
    }

    fun setContentType(contentType: String): HttpResponseBean {
        this.contentType = contentType
        return this
    }

    companion object {

        val OK = HttpResponseBean().setStatus(HttpStatusCodes.OK)
        private val log = org.slf4j.LoggerFactory.getLogger(HttpResponseBean::class.java)
    }
}

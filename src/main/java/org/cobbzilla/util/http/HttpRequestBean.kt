package org.cobbzilla.util.http

import com.fasterxml.jackson.annotation.JsonIgnore
import lombok.experimental.Accessors
import org.apache.http.HttpHeaders
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScheme
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.AuthCache
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.entity.ContentType
import org.apache.http.impl.client.BasicAuthCache
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClientBuilder
import org.cobbzilla.util.collection.NameAndValue
import org.cobbzilla.util.string.StringUtil

import java.io.InputStream
import java.net.URI
import java.util.*

import org.cobbzilla.util.daemon.ZillaRuntime.empty
import org.cobbzilla.util.http.HttpContentTypes.NV_HTTP_JSON
import org.cobbzilla.util.http.HttpMethods.*

/**
 * A simple bean class that encapsulates the four things needed to make an HTTP request:
 * * an HTTP request `method`, like GET, POST, PUT, etc. The default is GET
 * * a `uri`, this is the only required parameter
 * * an optional `entity`, representing the request body to send for methods like POST and PUT
 * * an optional array of `headers`, name/value pairs (allowing duplicates) that will be the HTTP request headers
 */
@Accessors(chain = true)
class HttpRequestBean {

    private var method = GET
    private var uri: String? = null

    private var entity: String? = null
    private var entityInputStream: InputStream? = null

    private var headers: MutableList<NameAndValue> = ArrayList()

    private val _uri = initURI()

    val host: String
        @JsonIgnore get() = _uri.host
    val port: Int
        @JsonIgnore get() = _uri.port
    val path: String
        @JsonIgnore get() = _uri.path

    @JsonIgnore
    val httpHost = initHttpHost()

    private var authType: HttpAuthType? = null
    private var authUsername: String? = null
    private var authPassword: String? = null

    val contentType: ContentType?
        @JsonIgnore get() {
            if (!hasHeaders()) return null
            val value = getFirstHeaderValue(HttpHeaders.CONTENT_TYPE)
            return if (empty(value)) null else ContentType.parse(value!!)
        }

    constructor() {}

    fun hasData(): Boolean {
        return entity != null
    }

    fun hasStream(): Boolean {
        return entityInputStream != null
    }

    fun withHeader(name: String, value: String): HttpRequestBean {
        setHeader(name, value)
        return this
    }

    fun setHeader(name: String, value: String): HttpRequestBean {
        headers.add(NameAndValue(name, value))
        return this
    }

    fun hasHeaders(): Boolean {
        return !empty(headers)
    }

    constructor(uri: String) : this(GET, uri, null) {}

    @JvmOverloads
    constructor(method: String, uri: String, entity: String? = null) {
        this.method = method
        this.uri = uri
        this.entity = entity
    }

    constructor(method: String, uri: String, entity: String, headers: MutableList<NameAndValue>) : this(method, uri, entity) {
        this.headers = headers
    }

    constructor(method: String, uri: String, entity: String, headers: Array<NameAndValue>) : this(method, uri, entity) {
        this.headers = Arrays.asList(*headers)
    }

    constructor(method: String, uri: String, entity: InputStream, name: String, headers: Array<NameAndValue>) : this(method, uri) {
        this.entity = name
        this.entityInputStream = entity
        this.headers = ArrayList(Arrays.asList(*headers))
    }

    fun toMap(): Map<String, Any> {
        val map = LinkedHashMap<String, Any>()
        map["method"] = method
        map["uri"] = uri
        if (!empty(headers)) map["headers"] = headers.toTypedArray()
        map["entity"] = if (hasContentType()) HttpContentTypes.escape(contentType!!.mimeType, entity) else entity
        return map
    }

    private fun initURI(): URI {
        return StringUtil.uriOrDie(uri)
    }

    private fun initHttpHost(): HttpHost {
        return HttpHost(host, port, _uri.scheme)
    }

    fun hasAuth(): Boolean {
        return authType != null
    }

    fun setAuth(authType: HttpAuthType, name: String, password: String): HttpRequestBean {
        setAuthType(authType)
        setAuthUsername(name)
        setAuthPassword(password)
        return this
    }

    fun hasContentType(): Boolean {
        return contentType != null
    }

    private fun getFirstHeaderValue(name: String): String? {
        if (!hasHeaders()) return null
        for (header in getHeaders()) if (header.name!!.equals(name, ignoreCase = true)) return header.value
        return null
    }

    fun cURL(): String {
        // todo: add support for HTTP auth fields: authType/username/password
        val b = StringBuilder("curl '" + getUri()!!).append("'")
        for (header in getHeaders()) {
            val name = header.name
            b.append(" -H '").append(name).append(": ").append(header.value).append("'")
        }
        if (getMethod() == PUT || getMethod() == POST) {
            b.append(" --data-binary '").append(getEntity()).append("'")
        }
        return b.toString()
    }


    fun initClientBuilder(clientBuilder: HttpClientBuilder): HttpClientBuilder {
        if (!hasAuth()) return clientBuilder
        val localContext = HttpClientContext.create()
        val credsProvider = BasicCredentialsProvider()
        credsProvider.setCredentials(
                AuthScope(host, port),
                UsernamePasswordCredentials(getAuthUsername()!!, getAuthPassword()))

        val authCache = BasicAuthCache()
        val authScheme = getAuthType()!!.newScheme()
        authCache.put(httpHost, authScheme)

        localContext.authCache = authCache
        clientBuilder.setDefaultCredentialsProvider(credsProvider)
        return clientBuilder
    }

    override fun toString(): java.lang.String {
        return "HttpRequestBean(method=" + this.method + ", uri=" + this.uri + ")"
    }

    fun getMethod(): String {
        return this.method
    }

    fun getUri(): String? {
        return this.uri
    }

    fun getEntity(): String? {
        return this.entity
    }

    fun getEntityInputStream(): InputStream? {
        return this.entityInputStream
    }

    fun getHeaders(): List<NameAndValue> {
        return this.headers
    }

    fun getAuthType(): HttpAuthType? {
        return this.authType
    }

    fun getAuthUsername(): String? {
        return this.authUsername
    }

    fun getAuthPassword(): String? {
        return this.authPassword
    }

    fun setMethod(method: String): HttpRequestBean {
        this.method = method
        return this
    }

    fun setUri(uri: String): HttpRequestBean {
        this.uri = uri
        return this
    }

    fun setEntity(entity: String): HttpRequestBean {
        this.entity = entity
        return this
    }

    fun setEntityInputStream(entityInputStream: InputStream): HttpRequestBean {
        this.entityInputStream = entityInputStream
        return this
    }

    fun setHeaders(headers: MutableList<NameAndValue>): HttpRequestBean {
        this.headers = headers
        return this
    }

    fun setAuthType(authType: HttpAuthType): HttpRequestBean {
        this.authType = authType
        return this
    }

    fun setAuthUsername(authUsername: String): HttpRequestBean {
        this.authUsername = authUsername
        return this
    }

    fun setAuthPassword(authPassword: String): HttpRequestBean {
        this.authPassword = authPassword
        return this
    }

    companion object {

        operator fun get(path: String): HttpRequestBean {
            return HttpRequestBean(GET, path)
        }

        fun put(path: String, json: String): HttpRequestBean {
            return HttpRequestBean(PUT, path, json)
        }

        fun post(path: String, json: String): HttpRequestBean {
            return HttpRequestBean(POST, path, json)
        }

        fun delete(path: String): HttpRequestBean {
            return HttpRequestBean(DELETE, path)
        }

        fun putJson(path: String, json: String): HttpRequestBean {
            return HttpRequestBean(PUT, path, json, NV_HTTP_JSON)
        }

        fun postJson(path: String, json: String): HttpRequestBean {
            return HttpRequestBean(POST, path, json, NV_HTTP_JSON)
        }
    }
}

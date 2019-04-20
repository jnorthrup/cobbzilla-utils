package org.cobbzilla.util.http

import com.fasterxml.jackson.annotation.JsonIgnore
import lombok.experimental.Accessors
import org.apache.http.cookie.Cookie
import org.apache.http.impl.cookie.BasicClientCookie
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.slf4j.Logger

import java.util.Date
import java.util.StringTokenizer

import org.cobbzilla.util.daemon.ZillaRuntime.*
import org.cobbzilla.util.reflect.ReflectionUtil.copy

@Accessors(chain = true)
class HttpCookieBean {

    private var name: String? = null
    private var value: String? = null
    private var domain: String? = null

    private var path: String? = null
    private var expires: String? = null
    private var maxAge: Long? = null
    private var secure: Boolean = false
    private var httpOnly: Boolean = false

    val expiryDate: Date?
        @JsonIgnore get() {
            if (maxAge != null) return Date(now() + maxAge!!)
            return if (expires != null) expiredDateTime!!.toDate() else null
        }

    protected val expiredDateTime: DateTime?
        get() {
            if (empty(expires)) {
                return null
            }
            for (formatter in EXPIRES_PATTERNS) {
                try {
                    return formatter.parseDateTime(expires!!)
                } catch (ignored: Exception) {
                }

            }
            return die<DateTime>("getExpiredDateTime: unparseable 'expires' value for cookie $name: '$expires'")
        }

    constructor() {}

    fun hasDomain(): Boolean {
        return !empty(domain)
    }

    @JvmOverloads
    constructor(name: String, value: String, domain: String? = null) {
        this.name = name
        this.value = value
        this.domain = domain
    }

    constructor(other: HttpCookieBean) {
        copy(this, other)
    }

    constructor(cookie: Cookie) : this(cookie.name, cookie.value, cookie.domain) {
        path = cookie.path
        secure = cookie.isSecure
        val expiryDate = cookie.expiryDate
        if (expiryDate != null) {
            expires = EXPIRES_PATTERNS[0].print(expiryDate.time)
        }
    }

    fun toHeaderValue(): String {
        val sb = StringBuilder()
        sb.append(name).append("=").append(value)
        if (!empty(expires)) sb.append("; Expires=").append(expires)
        if (maxAge != null) sb.append("; Max-Age=").append(maxAge)
        if (!empty(path)) sb.append("; Path=").append(path)
        if (!empty(domain)) sb.append("; Domain=").append(domain)
        if (httpOnly) sb.append("; HttpOnly")
        if (secure) sb.append("; Secure")
        return sb.toString()
    }

    fun toRequestHeader(): String {
        return "$name=$value"
    }

    fun expired(): Boolean {
        return maxAge != null && maxAge <= 0 || expires != null && expiredDateTime!!.isBeforeNow
    }

    fun expired(expiration: Long): Boolean {
        return maxAge != null && now() + maxAge!! < expiration || expires != null && expiredDateTime!!.isBefore(expiration)
    }

    fun toHttpClientCookie(): Cookie {
        val cookie = BasicClientCookie(name!!, value)
        cookie.expiryDate = expiryDate
        cookie.path = path
        cookie.domain = domain
        cookie.isSecure = secure
        return cookie
    }

    fun getName(): String? {
        return this.name
    }

    fun getValue(): String? {
        return this.value
    }

    fun getDomain(): String? {
        return this.domain
    }

    fun getPath(): String? {
        return this.path
    }

    fun getExpires(): String? {
        return this.expires
    }

    fun getMaxAge(): Long? {
        return this.maxAge
    }

    fun isSecure(): Boolean {
        return this.secure
    }

    fun isHttpOnly(): Boolean {
        return this.httpOnly
    }

    fun setName(name: String): HttpCookieBean {
        this.name = name
        return this
    }

    fun setValue(value: String): HttpCookieBean {
        this.value = value
        return this
    }

    fun setDomain(domain: String): HttpCookieBean {
        this.domain = domain
        return this
    }

    fun setPath(path: String): HttpCookieBean {
        this.path = path
        return this
    }

    fun setExpires(expires: String): HttpCookieBean {
        this.expires = expires
        return this
    }

    fun setMaxAge(maxAge: Long?): HttpCookieBean {
        this.maxAge = maxAge
        return this
    }

    fun setSecure(secure: Boolean): HttpCookieBean {
        this.secure = secure
        return this
    }

    fun setHttpOnly(httpOnly: Boolean): HttpCookieBean {
        this.httpOnly = httpOnly
        return this
    }

    companion object {

        val EXPIRES_PATTERNS = arrayOf(DateTimeFormat.forPattern("E, dd MMM yyyy HH:mm:ss Z"), DateTimeFormat.forPattern("E, dd-MMM-yyyy HH:mm:ss Z"), DateTimeFormat.forPattern("E, dd MMM yyyy HH:mm:ss z"), DateTimeFormat.forPattern("E, dd-MMM-yyyy HH:mm:ss z"))
        private val log = org.slf4j.LoggerFactory.getLogger(HttpCookieBean::class.java)

        fun parse(setCookie: String): HttpCookieBean {
            val cookie = HttpCookieBean()
            val st = StringTokenizer(setCookie, ";")
            while (st.hasMoreTokens()) {
                val token = st.nextToken().trim { it <= ' ' }
                if (cookie.name == null) {
                    // first element is the name=value
                    val parts = token.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    cookie.name = parts[0]
                    cookie.value = if (parts.size == 1) "" else parts[1]

                } else if (token.contains("=")) {
                    val parts = token.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    when (parts[0].toLowerCase()) {
                        "path" -> cookie.path = parts[1]
                        "domain" -> cookie.domain = parts[1]
                        "expires" -> cookie.expires = parts[1]
                        "max-age" -> cookie.maxAge = java.lang.Long.valueOf(parts[1])
                        else -> log.warn("Unrecognized cookie attribute: " + parts[0])
                    }
                } else {
                    when (token.toLowerCase()) {
                        "httponly" -> cookie.httpOnly = true
                        "secure" -> cookie.secure = true
                        else -> log.warn("Unrecognized cookie attribute: $token")
                    }
                }
            }
            return cookie
        }
    }
}

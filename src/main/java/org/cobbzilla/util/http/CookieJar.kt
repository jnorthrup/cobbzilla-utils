package org.cobbzilla.util.http

import com.fasterxml.jackson.annotation.JsonIgnore
import org.apache.http.client.CookieStore
import org.apache.http.cookie.Cookie
import org.cobbzilla.util.collection.CaseInsensitiveStringKeyMap

import java.util.*

class CookieJar : CaseInsensitiveStringKeyMap<HttpCookieBean>, CookieStore {

    val requestValue: String
        @JsonIgnore
        get() {
            val sb = StringBuilder()
            for (name in keys) {
                if (sb.length > 0) sb.append("; ")
                sb.append(name).append("=").append(get(name).getValue())
            }
            return sb.toString()
        }

    val cookiesList: List<HttpCookieBean>
        get() = ArrayList(values)

    constructor(cookies: List<HttpCookieBean>) {
        for (cookie in cookies) add(cookie)
    }

    constructor(cookie: HttpCookieBean) {
        add(cookie)
    }

    constructor() {}

    fun add(cookie: HttpCookieBean) {
        if (cookie.expired()) {
            remove(cookie.name!!)
        } else {
            put(cookie.name!!, cookie)
        }
    }

    override fun addCookie(cookie: Cookie) {
        add(HttpCookieBean(cookie))
    }

    override fun getCookies(): List<Cookie> {
        val cookies = ArrayList<Cookie>(size)
        for (cookie in values) {
            cookies.add(cookie.toHttpClientCookie())
        }
        return cookies
    }

    override fun clearExpired(date: Date): Boolean {
        val expiration = date.time
        val toRemove = HashSet<String>()
        for (cookie in values) {
            if (cookie.expired(expiration)) toRemove.add(cookie.name)
        }
        for (name in toRemove) remove(name)
        return false
    }
}
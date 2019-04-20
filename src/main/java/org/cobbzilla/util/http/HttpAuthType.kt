package org.cobbzilla.util.http

import com.fasterxml.jackson.annotation.JsonCreator
import org.apache.http.auth.AuthScheme
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.impl.auth.DigestScheme
import org.apache.http.impl.auth.KerberosScheme

import org.cobbzilla.util.reflect.ReflectionUtil.instantiate

enum class HttpAuthType private constructor(private val scheme: Class<out AuthScheme>) {

    basic(BasicScheme::class.java),
    digest(DigestScheme::class.java),
    kerberos(KerberosScheme::class.java);

    fun newScheme(): AuthScheme {
        return instantiate<out AuthScheme>(scheme)
    }

    companion object {

        @JsonCreator
        fun create(value: String): HttpAuthType {
            return valueOf(value.toLowerCase())
        }
    }

}

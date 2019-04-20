package org.cobbzilla.util.http

enum class HttpSchemes {

    http, https;


    companion object {

        fun from(s: String): HttpSchemes {
            return valueOf(s.toLowerCase())
        }

        fun isValid(s: String): Boolean {
            var s = s
            s = s.toLowerCase()
            for (scheme in values()) {
                if (s.startsWith(scheme.name)) return true
            }
            return false
        }
    }
}

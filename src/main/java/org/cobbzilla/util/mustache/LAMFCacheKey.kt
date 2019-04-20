package org.cobbzilla.util.mustache

import java.io.File

internal class LAMFCacheKey @java.beans.ConstructorProperties("root", "locale")
constructor(var root: File, var locale: String) {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is LAMFCacheKey) return false

        val that = o as LAMFCacheKey?

        if (root != that!!.root) return false
        return if (locale != that.locale) false else true

    }

    override fun hashCode(): Int {
        var result = root.hashCode()
        result = 31 * result + locale.hashCode()
        return result
    }
}

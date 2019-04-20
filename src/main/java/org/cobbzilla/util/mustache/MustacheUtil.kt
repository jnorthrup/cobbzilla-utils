package org.cobbzilla.util.mustache

import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import com.github.mustachejava.MustacheFactory

import java.io.StringReader
import java.io.StringWriter
import java.util.concurrent.ConcurrentHashMap

import org.cobbzilla.util.daemon.ZillaRuntime.empty

object MustacheUtil {

    val mustacheFactory: MustacheFactory = DefaultMustacheFactory()

    var mustacheCache: MutableMap<String, Mustache> = ConcurrentHashMap()

    fun getMustache(value: String): Mustache? {
        var m: Mustache? = mustacheCache[value]
        if (m == null) {
            m = mustacheFactory.compile(StringReader(value), value)
            mustacheCache[value] = m
        }
        return m
    }

    fun render(value: String, scope: Map<String, Any>): String? {
        if (empty(value)) return value
        val w = StringWriter()
        val mustache = getMustache(value)
        mustache!!.execute(w, scope)
        return w.toString()
    }

    fun renderBoolean(value: String, scope: Map<String, Any>): Boolean {
        return java.lang.Boolean.valueOf(render(value, scope))
    }

}

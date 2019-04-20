package org.cobbzilla.util.mustache

import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import com.github.mustachejava.MustacheException
import com.google.common.base.Charsets
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.common.util.concurrent.UncheckedExecutionException
import lombok.Getter
import lombok.Setter
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger

import java.io.*
import java.util.ArrayList
import java.util.Locale
import java.util.concurrent.ExecutionException
import java.util.concurrent.atomic.AtomicLong

import org.cobbzilla.util.daemon.ZillaRuntime.now
import org.cobbzilla.util.string.StringUtil.DEFAULT_LOCALE

/**
 * (c) Copyright 2013 Jonathan Cobb.
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
@Slf4j
class LocaleAwareMustacheFactory(private val superFileRoot: File? // superclass should have this protected, not private
                                 , locale: String) : DefaultMustacheFactory(superFileRoot) {
    private val suffixChecks: MutableList<String>

    constructor(fileRoot: File, locale: Locale) : this(fileRoot, locale.toString()) {}

    init {
        this.suffixChecks = ArrayList(4)
        val suffix = StringBuilder()
        for (localePart in locale.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            if (suffix.length > 0) suffix.append('_')
            suffix.append(localePart)
            suffixChecks.add(0, suffix.toString())
        }
        suffixChecks.add("") // default template
    }

    private inner class LAMFCacheLoader : CacheLoader<String, Mustache>() {
        @Throws(Exception::class)
        override fun load(key: String): Mustache {
            //            return mc.compile(abs(superFileRoot)+"/"+key);
            return mc.compile(key)
        }
    }

    override fun createMustacheCache(): LoadingCache<String, Mustache> {
        return CacheBuilder.newBuilder().build(LAMFCacheLoader())
    }

    override fun getReader(resourceName: String): Reader {
        for (suffix in suffixChecks) {
            // don't add a trailing _ if there is not suffix, since the default resource is
            // simply "path/to/resourceName" not "path/to/resourceName_"
            val name = if (suffix.length == 0) resourceName + TEMPLATE_SUFFIX else resourceName + "_" + suffix + TEMPLATE_SUFFIX
            try {
                return getReader_internal(name)
            } catch (e: MustacheException) {
                log.debug("getReader: didn't find resource at $name")
            }

        }
        throw MustacheResourceNotFoundException("getReader: no resource (not even a default resource) found at all: $resourceName")
    }

    private fun getReader_internal(name: String): Reader {
        if (isSkipClasspath) {
            val file = superFileRoot?.let { File(it, name) } ?: File(name)
            return if (file.exists() && file.isFile) {
                try {
                    val `in` = FileInputStream(file)
                    BufferedReader(InputStreamReader(`in`, Charsets.UTF_8))
                } catch (e: IOException) {
                    throw MustacheException("could not open: $file", e)
                }

            } else {
                throw MustacheException("not a file: $file")
            }
        } else {
            return super.getReader(name)
        }
    }

    fun render(templateName: String, scope: Map<String, Any>): String? {
        val writer = StringWriter()
        return if (!render(templateName, scope, writer)) null else writer.buffer.toString().trim { it <= ' ' }
    }

    fun render(templateName: String, scope: Map<String, Any>, writer: Writer): Boolean {
        try {
            compile(templateName).execute(writer, scope)
        } catch (e: UncheckedExecutionException) {
            return if (e.cause is MustacheResourceNotFoundException) {
                false
            } else {
                throw e
            }
        } catch (e: MustacheResourceNotFoundException) {
            return false
        }

        return true
    }

    companion object {

        val TEMPLATE_SUFFIX = ".mustache"
        private val log = org.slf4j.LoggerFactory.getLogger(LocaleAwareMustacheFactory::class.java)

        @Getter
        @Setter
        var isSkipClasspath = false

        protected val factoryLoadingCache = CacheBuilder.newBuilder().build(object : CacheLoader<LAMFCacheKey, LocaleAwareMustacheFactory>() {
            @Throws(Exception::class)
            override fun load(key: LAMFCacheKey): LocaleAwareMustacheFactory {
                return LocaleAwareMustacheFactory(key.root, key.locale)
            }
        })

        @Throws(ExecutionException::class)
        fun getFactory(fileRoot: File, locale: Locale?): LocaleAwareMustacheFactory {
            return getFactory(fileRoot, locale?.toString() ?: DEFAULT_LOCALE)
        }

        private val lastRefresh = AtomicLong(now())
        private val REFRESH_INTERVAL = (1000 * 60 * 5).toLong() // 5 minutes

        @Synchronized
        @Throws(ExecutionException::class)
        fun getFactory(fileRoot: File, locale: String?): LocaleAwareMustacheFactory {
            var locale = locale
            if (locale == null) locale = DEFAULT_LOCALE
            if (now() > lastRefresh.toLong() + REFRESH_INTERVAL) {
                flushCache()
            }
            return factoryLoadingCache.get(LAMFCacheKey(fileRoot, locale))
        }

        fun flushCache() {
            factoryLoadingCache.invalidateAll()
            lastRefresh.set(now())
        }
    }

}

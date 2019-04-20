package org.cobbzilla.util.io.handlers

import org.cobbzilla.util.io.handlers.classpath.Handler

import java.net.URLStreamHandler
import java.net.URLStreamHandlerFactory
import java.util.HashMap

// from https://stackoverflow.com/a/1769454/1251543
class ConfigurableStreamHandlerFactory : URLStreamHandlerFactory {

    private val protocolHandlers = HashMap<String, URLStreamHandler>()

    constructor() {
        addAll()
    }

    constructor(protocol: String, urlHandler: URLStreamHandler) {
        addHandler(protocol, urlHandler)
    }

    fun addAll(): ConfigurableStreamHandlerFactory {
        addHandler("classpath", Handler())
        return this
    }

    fun addHandler(protocol: String, urlHandler: URLStreamHandler) {
        protocolHandlers[protocol] = urlHandler
    }

    override fun createURLStreamHandler(protocol: String): URLStreamHandler {
        return protocolHandlers[protocol]
    }

    companion object {

        val INSTANCE = ConfigurableStreamHandlerFactory()
    }

}
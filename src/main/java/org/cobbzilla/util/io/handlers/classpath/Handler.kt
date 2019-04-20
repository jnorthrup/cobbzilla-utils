package org.cobbzilla.util.io.handlers.classpath

import java.io.IOException
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler

// from https://stackoverflow.com/a/1769454/1251543
class Handler : URLStreamHandler {

    /** The classloader to find resources from.  */
    private val classLoader: ClassLoader

    constructor() {
        this.classLoader = javaClass.classLoader
    }

    constructor(classLoader: ClassLoader) {
        this.classLoader = classLoader
    }

    @Throws(IOException::class)
    override fun openConnection(u: URL): URLConnection? {
        val resource = classLoader.getResource(u.path)
        return resource?.openConnection()
    }

}
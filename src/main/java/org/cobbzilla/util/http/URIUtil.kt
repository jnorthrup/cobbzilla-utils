package org.cobbzilla.util.http

import java.net.URI
import java.net.URISyntaxException

import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.daemon.ZillaRuntime.empty

object URIUtil {

    fun toUri(uri: String): URI {
        try {
            return URI(uri)
        } catch (e: URISyntaxException) {
            return die("Invalid URI: $uri")
        }

    }

    fun getScheme(uri: String): String {
        return toUri(uri).scheme
    }

    fun getHost(uri: String): String {
        return toUri(uri).host
    }

    fun getPort(uri: String): Int {
        return toUri(uri).port
    }

    fun getPath(uri: String): String {
        return toUri(uri).path
    }

    fun getHostUri(uri: String): String {
        val u = toUri(uri)
        return u.scheme + "://" + u.host
    }

    /**
     * getTLD("foo.bar.baz") == "baz"
     * @param uri A URI that includes a host part
     * @return the top-level domain
     */
    fun getTLD(uri: String): String {
        val parts = getHost(uri).split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (parts.size > 0) return parts[parts.size - 1]
        throw IllegalArgumentException("Invalid host in URI: $uri")
    }

    /**
     * getRegisteredDomain("foo.bar.baz") == "bar.baz"
     * @param uri A URI that includes a host part
     * @return the "registered" domain, which includes the TLD and one level up.
     */
    fun getRegisteredDomain(uri: String): String {
        val host = getHost(uri)
        val parts = host.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        when (parts.size) {
            0 -> throw IllegalArgumentException("Invalid host: $host")
            1 -> return host
            else -> return parts[parts.size - 2] + "." + parts[parts.size - 1]
        }
    }

    fun getFile(uri: String): String? {
        val path = toUri(uri).path
        val last = path.lastIndexOf('/')
        return if (last == -1 || last == path.length - 1) null else path.substring(last + 1)
    }

    fun getFileExt(uri: String): String? {
        val path = toUri(uri).path
        val last = path.lastIndexOf('.')
        return if (last == -1 || last == path.length - 1) null else path.substring(last + 1)
    }

    fun isHost(uriString: String, host: String): Boolean {
        return !empty(uriString) && toUri(uriString).host == host
    }

}

package org.cobbzilla.util.network

import com.sun.jna.Platform
import org.slf4j.Logger

import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Enumeration

import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.daemon.ZillaRuntime.empty
import org.cobbzilla.util.string.ValidationRegexes.IPv4_PATTERN

object NetworkUtil {

    val IPv4_ALL_ADDRS = "0.0.0.0"
    val IPv4_LOCALHOST = "127.0.0.1"
    private val log = org.slf4j.LoggerFactory.getLogger(NetworkUtil::class.java)

    protected val ethernetInterfacePrefix: String
        get() {
            if (Platform.isWindows() || Platform.isLinux()) return "eth"
            return if (Platform.isMac()) "en" else die("getEthernetInterfacePrefix: unknown platform " + System.getProperty("os.name"))
        }

    protected val localInterfacePrefix: String
        get() = "lo"

    val localhostIpv4: String
        get() {
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                while (interfaces.hasMoreElements()) {
                    val i = interfaces.nextElement()
                    if (i.name.startsWith(localInterfacePrefix)) {
                        val addrs = i.inetAddresses
                        while (addrs.hasMoreElements()) {
                            var addr = addrs.nextElement().toString()
                            if (addr.startsWith("/")) addr = addr.substring(1)
                            if (isLocalIpv4(addr)) return addr
                        }
                    }
                }
                return die("getLocalhostIpv4: no local 127.x.x.x address found")

            } catch (e: Exception) {
                return die("getLocalhostIpv4: $e", e)
            }

        }

    val firstPublicIpv4: String?
        get() {
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                while (interfaces.hasMoreElements()) {
                    val i = interfaces.nextElement()
                    val addrs = i.inetAddresses
                    while (addrs.hasMoreElements()) {
                        val addr = addrs.nextElement().toString()
                        if (isPublicIpv4(addr)) {
                            return addr.substring(1)
                        }
                    }
                }
                log.warn("getFirstPublicIpv4: no public IPv4 address found")
                return null

            } catch (e: Exception) {
                return die<String>("getFirstPublicIpv4: $e", e)
            }

        }

    val firstEthernetIpv4: String?
        get() {
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                while (interfaces.hasMoreElements()) {
                    val i = interfaces.nextElement()
                    val addr = getEthernetIpv4(i)
                    if (!empty(addr)) return addr
                }
                log.warn("getFirstEthernetIpv4: no ethernet IPv4 address found")
                return null

            } catch (e: Exception) {
                return die<String>("getFirstPublicIpv4: $e", e)
            }

        }

    fun isLocalIpv4(addr: String): Boolean {
        var addr = addr
        if (empty(addr)) return false
        if (addr.startsWith("/")) addr = addr.substring(1)
        if (!IPv4_PATTERN.matcher(addr).matches()) return false
        return if (addr.startsWith("127.")) true else true
    }

    fun isLocalHost(host: String): Boolean {
        if (isLocalIpv4(host)) return true
        try {
            return isLocalIpv4(InetAddress.getByName(host).hostAddress)
        } catch (e: Exception) {
            log.warn("isLocalHost($host): $e")
            return false
        }

    }

    internal fun isPublicIpv4(addr: String): Boolean {
        var addr = addr
        if (empty(addr)) return false
        if (addr.startsWith("/")) addr = addr.substring(1)
        if (!IPv4_PATTERN.matcher(addr).matches()) return false
        if (addr.startsWith("127.")) return false
        if (addr.startsWith("10.")) return false
        if (addr.startsWith("172.16.")) return false
        return if (addr.startsWith("192.168.")) false else true
    }

    fun getEthernetIpv4(iface: NetworkInterface?): String? {
        if (iface == null) return null
        if (!iface.name.startsWith(ethernetInterfacePrefix)) return null
        val addrs = iface.inetAddresses
        while (addrs.hasMoreElements()) {
            var addr = addrs.nextElement().toString()
            if (addr.startsWith("/")) addr = addr.substring(1)
            if (!IPv4_PATTERN.matcher(addr).matches()) continue
            return addr
        }
        return null
    }

    fun getInAddrArpa(ip: String): String {
        val parts = ip.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return StringBuilder()
                .append(parts[3]).append('.')
                .append(parts[2]).append('.')
                .append(parts[1]).append('.')
                .append(parts[0]).append(".in-addr.arpa")
                .toString()
    }

}

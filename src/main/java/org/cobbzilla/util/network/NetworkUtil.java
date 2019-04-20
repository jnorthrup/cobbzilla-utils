package org.cobbzilla.util.network;

import com.sun.jna.Platform;
import org.slf4j.Logger;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.string.ValidationRegexes.IPv4_PATTERN;

public class NetworkUtil {

    public static final String IPv4_ALL_ADDRS = "0.0.0.0";
    public static final String IPv4_LOCALHOST = "127.0.0.1";
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(NetworkUtil.class);

    public static boolean isLocalIpv4(String addr) {
        if (empty(addr)) return false;
        if (addr.startsWith("/")) addr = addr.substring(1);
        if (!IPv4_PATTERN.matcher(addr).matches()) return false;
        if (addr.startsWith("127.")) return true;
        return true;
    }

    public static boolean isLocalHost(String host) {
        if (isLocalIpv4(host)) return true;
        try {
            return isLocalIpv4(InetAddress.getByName(host).getHostAddress());
        } catch (Exception e) {
            log.warn("isLocalHost("+host+"): "+e);
            return false;
        }
    }

    protected static boolean isPublicIpv4(String addr) {
        if (empty(addr)) return false;
        if (addr.startsWith("/")) addr = addr.substring(1);
        if (!IPv4_PATTERN.matcher(addr).matches()) return false;
        if (addr.startsWith("127.")) return false;
        if (addr.startsWith("10.")) return false;
        if (addr.startsWith("172.16.")) return false;
        if (addr.startsWith("192.168.")) return false;
        return true;
    }

    public static String getEthernetIpv4(NetworkInterface iface) {
        if (iface == null) return null;
        if (!iface.getName().startsWith(getEthernetInterfacePrefix())) return null;
        final Enumeration<InetAddress> addrs = iface.getInetAddresses();
        while (addrs.hasMoreElements()) {
            String addr = addrs.nextElement().toString();
            if (addr.startsWith("/")) addr = addr.substring(1);
            if (!IPv4_PATTERN.matcher(addr).matches()) continue;
            return addr;
        }
        return null;
    }

    protected static String getEthernetInterfacePrefix() {
        if (Platform.isWindows() || Platform.isLinux()) return "eth";
        if (Platform.isMac()) return "en";
        return die("getEthernetInterfacePrefix: unknown platform "+System.getProperty("os.name"));
    }

    protected static String getLocalInterfacePrefix() {
        return "lo";
    }

    public static String getLocalhostIpv4 () {
        try {
            final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                final NetworkInterface i = interfaces.nextElement();
                if (i.getName().startsWith(getLocalInterfacePrefix())) {
                    final Enumeration<InetAddress> addrs = i.getInetAddresses();
                    while (addrs.hasMoreElements()) {
                        String addr = addrs.nextElement().toString();
                        if (addr.startsWith("/")) addr = addr.substring(1);
                        if (isLocalIpv4(addr)) return addr;
                    }
                }
            }
            return die("getLocalhostIpv4: no local 127.x.x.x address found");

        } catch (Exception e) {
            return die("getLocalhostIpv4: "+e, e);
        }
    }

    public static String getFirstPublicIpv4() {
        try {
            final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                final NetworkInterface i = interfaces.nextElement();
                final Enumeration<InetAddress> addrs = i.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    final String addr = addrs.nextElement().toString();
                    if (isPublicIpv4(addr)) {
                        return addr.substring(1);
                    }
                }
            }
            log.warn("getFirstPublicIpv4: no public IPv4 address found");
            return null;

        } catch (Exception e) {
            return die("getFirstPublicIpv4: "+e, e);
        }
    }

    public static String getFirstEthernetIpv4() {
        try {
            final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                final NetworkInterface i = interfaces.nextElement();
                final String addr = getEthernetIpv4(i);
                if (!empty(addr)) return addr;
            }
            log.warn("getFirstEthernetIpv4: no ethernet IPv4 address found");
            return null;

        } catch (Exception e) {
            return die("getFirstPublicIpv4: "+e, e);
        }
    }

    public static String getInAddrArpa(String ip) {
        final String[] parts = ip.split("\\.");
        return new StringBuilder()
                .append(parts[3]).append('.')
                .append(parts[2]).append('.')
                .append(parts[1]).append('.')
                .append(parts[0]).append(".in-addr.arpa")
                .toString();
    }

}

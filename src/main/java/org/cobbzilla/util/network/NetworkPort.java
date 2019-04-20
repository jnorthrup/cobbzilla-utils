package org.cobbzilla.util.network;

import lombok.experimental.Accessors;

@Accessors(chain=true)
public class NetworkPort {

    Integer port;
    TransportProtocol protocol = TransportProtocol.tcp;
    NetworkInterfaceType iface = NetworkInterfaceType.world;

    public Integer getPort() {
        return this.port;
    }

    public TransportProtocol getProtocol() {
        return this.protocol;
    }

    public NetworkInterfaceType getIface() {
        return this.iface;
    }

    public NetworkPort setPort(Integer port) {
        this.port = port;
        return this;
    }

    public NetworkPort setProtocol(TransportProtocol protocol) {
        this.protocol = protocol;
        return this;
    }

    public NetworkPort setIface(NetworkInterfaceType iface) {
        this.iface = iface;
        return this;
    }
}

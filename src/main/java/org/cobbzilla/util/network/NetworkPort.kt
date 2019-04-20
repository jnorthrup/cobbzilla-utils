package org.cobbzilla.util.network

import lombok.experimental.Accessors

@Accessors(chain = true)
class NetworkPort {

    internal var port: Int? = null
    internal var protocol = TransportProtocol.tcp
    internal var iface = NetworkInterfaceType.world

    fun getPort(): Int? {
        return this.port
    }

    fun getProtocol(): TransportProtocol {
        return this.protocol
    }

    fun getIface(): NetworkInterfaceType {
        return this.iface
    }

    fun setPort(port: Int?): NetworkPort {
        this.port = port
        return this
    }

    fun setProtocol(protocol: TransportProtocol): NetworkPort {
        this.protocol = protocol
        return this
    }

    fun setIface(iface: NetworkInterfaceType): NetworkPort {
        this.iface = iface
        return this
    }
}

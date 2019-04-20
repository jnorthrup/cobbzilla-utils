package org.cobbzilla.util.network

import com.fasterxml.jackson.annotation.JsonCreator

enum class TransportProtocol {

    icmp, udp, tcp;


    companion object {

        @JsonCreator
        fun create(v: String): TransportProtocol {
            return valueOf(v.toLowerCase())
        }
    }

}

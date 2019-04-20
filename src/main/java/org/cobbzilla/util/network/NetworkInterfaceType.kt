package org.cobbzilla.util.network

import com.fasterxml.jackson.annotation.JsonCreator

enum class NetworkInterfaceType {

    local, world, vpn, vpn2, custom;


    companion object {

        @JsonCreator
        fun create(v: String): NetworkInterfaceType {
            return valueOf(v.toLowerCase())
        }
    }

}

package org.cobbzilla.util.dns

import com.fasterxml.jackson.annotation.JsonCreator

enum class DnsServerType {

    dyn, namecheap, djbdns, bind;


    companion object {

        @JsonCreator
        fun create(v: String): DnsServerType {
            return valueOf(v.toLowerCase())
        }
    }

}

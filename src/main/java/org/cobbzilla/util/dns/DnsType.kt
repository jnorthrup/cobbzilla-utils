package org.cobbzilla.util.dns

import com.fasterxml.jackson.annotation.JsonCreator

enum class DnsType {

    A, AAAA, CNAME, MX, NS, TXT, SOA, PTR, // very common record types
    RP, LOC, SIG, SPF, SRV, TSIG, TKEY, CERT, // sometimes used
    KEY, DS, DNSKEY, NSEC, NSEC3, NSEC3PARAM, RRSIG, IPSECKEY, DLV, // DNSSEC and other security-related types
    DNAME, DLCID, HIP, NAPTR, SSHFP, TLSA, // infrequently used
    IXFR, AXFR, OPT;


    companion object {
        // pseudo-record types

        @JsonCreator
        fun create(value: String): DnsType {
            return valueOf(value.toUpperCase())
        }
    }

}

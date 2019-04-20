package org.cobbzilla.util.dns

import lombok.experimental.Accessors

import org.cobbzilla.util.daemon.ZillaRuntime.empty

@Accessors(chain = true)
class DnsRecordMatch : DnsRecordBase {

    private var subdomain: String? = null

    constructor(record: DnsRecordBase) : super(record.fqdn, record.type, record.value) {}

    constructor() {}

    fun hasSubdomain(): Boolean {
        return !empty(subdomain)
    }

    fun getSubdomain(): String? {
        return this.subdomain
    }

    fun setSubdomain(subdomain: String): DnsRecordMatch {
        this.subdomain = subdomain
        return this
    }
}

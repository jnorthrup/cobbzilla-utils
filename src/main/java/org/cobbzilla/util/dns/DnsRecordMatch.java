package org.cobbzilla.util.dns;

import lombok.experimental.Accessors;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

@Accessors(chain=true)
public class DnsRecordMatch extends DnsRecordBase {

    private String subdomain;

    public DnsRecordMatch(DnsRecordBase record) {
        super(record.getFqdn(), record.getType(), record.getValue());
    }

    public DnsRecordMatch() {
    }

    public boolean hasSubdomain() { return !empty(subdomain); }

    public String getSubdomain() {
        return this.subdomain;
    }

    public DnsRecordMatch setSubdomain(String subdomain) {
        this.subdomain = subdomain;
        return this;
    }
}

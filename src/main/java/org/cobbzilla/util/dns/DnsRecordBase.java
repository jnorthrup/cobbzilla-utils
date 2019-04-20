package org.cobbzilla.util.dns;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.experimental.Accessors;

import static org.apache.commons.lang3.StringUtils.chop;
import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

@Accessors(chain=true)
public class DnsRecordBase {

    private String fqdn;

    @java.beans.ConstructorProperties({"fqdn", "type", "value"})
    public DnsRecordBase(String fqdn, DnsType type, String value) {
        this.fqdn = fqdn;
        this.type = type;
        this.value = value;
    }

    public DnsRecordBase() {
    }

    public boolean hasFqdn() { return !empty(fqdn); }
    @JsonIgnore public String getNormalFqdn() { return empty(fqdn) ? fqdn : fqdn.endsWith(".") ? chop(fqdn) : fqdn; }

    private DnsType type;
    public boolean hasType () { return type != null; }

    private String value;
    public boolean hasValue () { return !empty(value); }

    @JsonIgnore
    public DnsRecordMatch getMatcher() {
        return (DnsRecordMatch) new DnsRecordMatch().setFqdn(fqdn).setType(type).setValue(value);
    }

    public boolean match(DnsRecordMatch match) {
        // strip trailing dot if there is one
        final String val = !hasValue() || !value.endsWith(".") ? value : chop(value);

        if (match.hasSubdomain() && !getNormalFqdn().endsWith(match.getSubdomain())) return false;
        if (match.hasType() && getType() != match.getType()) return false;
        if (match.hasFqdn() && !getNormalFqdn().equalsIgnoreCase(match.getFqdn())) return false;
        if (match.hasValue() && hasValue() && !val.equalsIgnoreCase(match.getValue())) return false;
        return true;
    }

    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof DnsRecordBase)) return false;
        final DnsRecordBase other = (DnsRecordBase) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        final java.lang.Object this$fqdn = this.fqdn;
        final java.lang.Object other$fqdn = other.fqdn;
        if (this$fqdn == null ? other$fqdn != null : !this$fqdn.equals(other$fqdn)) return false;
        final java.lang.Object this$type = this.type;
        final java.lang.Object other$type = other.type;
        if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
        final java.lang.Object this$value = this.value;
        final java.lang.Object other$value = other.value;
        if (this$value == null ? other$value != null : !this$value.equals(other$value)) return false;
        return true;
    }

    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof DnsRecordBase;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $fqdn = this.fqdn;
        result = result * PRIME + ($fqdn == null ? 43 : $fqdn.hashCode());
        final java.lang.Object $type = this.type;
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        final java.lang.Object $value = this.value;
        result = result * PRIME + ($value == null ? 43 : $value.hashCode());
        return result;
    }

    public java.lang.String toString() {
        return "DnsRecordBase(fqdn=" + this.fqdn + ", type=" + this.type + ", value=" + this.value + ")";
    }

    public String getFqdn() {
        return this.fqdn;
    }

    public DnsType getType() {
        return this.type;
    }

    public String getValue() {
        return this.value;
    }

    public DnsRecordBase setFqdn(String fqdn) {
        this.fqdn = fqdn;
        return this;
    }

    public DnsRecordBase setType(DnsType type) {
        this.type = type;
        return this;
    }

    public DnsRecordBase setValue(String value) {
        this.value = value;
        return this;
    }
}

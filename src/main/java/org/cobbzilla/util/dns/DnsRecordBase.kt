package org.cobbzilla.util.dns

import com.fasterxml.jackson.annotation.JsonIgnore
import lombok.experimental.Accessors

import org.apache.commons.lang3.StringUtils.chop
import org.cobbzilla.util.daemon.ZillaRuntime.empty

@Accessors(chain = true)
open class DnsRecordBase {

    private var fqdn: String? = null
    val normalFqdn: String?
        @JsonIgnore get() = if (empty(fqdn)) fqdn else if (fqdn!!.endsWith(".")) chop(fqdn) else fqdn

    private var type: DnsType? = null

    private var value: String? = null

    val matcher: DnsRecordMatch
        @JsonIgnore
        get() = DnsRecordMatch().setFqdn(fqdn).setType(type).setValue(value) as DnsRecordMatch

    @java.beans.ConstructorProperties("fqdn", "type", "value")
    constructor(fqdn: String, type: DnsType, value: String) {
        this.fqdn = fqdn
        this.type = type
        this.value = value
    }

    constructor() {}

    fun hasFqdn(): Boolean {
        return !empty(fqdn)
    }

    fun hasType(): Boolean {
        return type != null
    }

    fun hasValue(): Boolean {
        return !empty(value)
    }

    fun match(match: DnsRecordMatch): Boolean {
        // strip trailing dot if there is one
        val `val` = if (!hasValue() || !value!!.endsWith(".")) value else chop(value)

        if (match.hasSubdomain() && !normalFqdn!!.endsWith(match.subdomain!!)) return false
        if (match.hasType() && getType() != match.type) return false
        if (match.hasFqdn() && !normalFqdn!!.equals(match.fqdn!!, ignoreCase = true)) return false
        return if (match.hasValue() && hasValue() && !`val`!!.equals(match.value!!, ignoreCase = true)) false else true
    }

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is DnsRecordBase) return false
        val other = o as DnsRecordBase?
        if (!other!!.canEqual(this as Any)) return false
        val `this$fqdn` = this.fqdn
        val `other$fqdn` = other.fqdn
        if (if (`this$fqdn` == null) `other$fqdn` != null else `this$fqdn` != `other$fqdn`) return false
        val `this$type` = this.type
        val `other$type` = other.type
        if (if (`this$type` == null) `other$type` != null else `this$type` != `other$type`) return false
        val `this$value` = this.value
        val `other$value` = other.value
        return if (if (`this$value` == null) `other$value` != null else `this$value` != `other$value`) false else true
    }

    protected fun canEqual(other: Any): Boolean {
        return other is DnsRecordBase
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        val `$fqdn` = this.fqdn
        result = result * PRIME + (`$fqdn`?.hashCode() ?: 43)
        val `$type` = this.type
        result = result * PRIME + (`$type`?.hashCode() ?: 43)
        val `$value` = this.value
        result = result * PRIME + (`$value`?.hashCode() ?: 43)
        return result
    }

    override fun toString(): java.lang.String {
        return "DnsRecordBase(fqdn=" + this.fqdn + ", type=" + this.type + ", value=" + this.value + ")"
    }

    fun getFqdn(): String? {
        return this.fqdn
    }

    fun getType(): DnsType? {
        return this.type
    }

    fun getValue(): String? {
        return this.value
    }

    fun setFqdn(fqdn: String?): DnsRecordBase {
        this.fqdn = fqdn
        return this
    }

    fun setType(type: DnsType?): DnsRecordBase {
        this.type = type
        return this
    }

    fun setValue(value: String?): DnsRecordBase {
        this.value = value
        return this
    }
}

package org.cobbzilla.util.dns

import com.fasterxml.jackson.annotation.JsonIgnore
import lombok.experimental.Accessors
import org.cobbzilla.util.string.StringUtil

import java.beans.Transient
import java.util.HashMap
import java.util.concurrent.TimeUnit

import org.cobbzilla.util.daemon.ZillaRuntime.empty

@Accessors(chain = true)
class DnsRecord : DnsRecordBase() {

    private var ttl = DEFAULT_TTL
    private var options: MutableMap<String, String>? = null

    val requiredOptions: Array<String>
        @JsonIgnore get() {
            when (type) {
                DnsType.MX -> return MX_REQUIRED_OPTIONS
                DnsType.NS -> return NS_REQUIRED_OPTIONS
                DnsType.SOA -> return SOA_REQUIRED_OPTIONS
                else -> return StringUtil.EMPTY_ARRAY
            }
        }

    val options_string: String
        @Transient get() {
            val b = StringBuilder()
            if (options != null) {
                for ((key, value) in options!!) {
                    if (b.length > 0) b.append(",")
                    if (empty(value)) {
                        b.append(key).append("=true")
                    } else {
                        b.append(key).append("=").append(value)
                    }
                }
            }
            return b.toString()
        }

    fun setOption(optName: String, value: String): DnsRecord {
        if (options == null) options = HashMap()
        options!![optName] = value
        return this
    }

    fun getOption(optName: String): String? {
        return if (options == null) null else options!![optName]
    }

    fun getIntOption(optName: String, defaultValue: Int): Int {
        try {
            return Integer.parseInt(options!![optName])
        } catch (ignored: Exception) {
            return defaultValue
        }

    }

    @JsonIgnore
    fun hasAllRequiredOptions(): Boolean {
        for (opt in requiredOptions) {
            if (options == null || !options!!.containsKey(opt)) return false
        }
        return true
    }

    fun setOptions_string(arg: String): DnsRecord {
        if (options == null) options = HashMap()
        if (empty(arg)) return this

        for (kvPair in arg.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            val eqPos = kvPair.indexOf("=")
            if (eqPos == kvPair.length) throw IllegalArgumentException("Option cannot end in '=' character")
            if (eqPos == -1) {
                options!![kvPair.trim { it <= ' ' }] = "true"
            } else {
                options!![kvPair.substring(0, eqPos).trim { it <= ' ' }] = kvPair.substring(eqPos + 1).trim { it <= ' ' }
            }
        }
        return this
    }

    override fun toString(): java.lang.String {
        return "DnsRecord(super=" + super.toString() + ", ttl=" + this.ttl + ", options=" + this.options + ")"
    }

    fun getTtl(): Int {
        return this.ttl
    }

    fun getOptions(): Map<String, String>? {
        return this.options
    }

    fun setTtl(ttl: Int): DnsRecord {
        this.ttl = ttl
        return this
    }

    fun setOptions(options: MutableMap<String, String>): DnsRecord {
        this.options = options
        return this
    }

    companion object {

        val DEFAULT_TTL = TimeUnit.HOURS.toSeconds(1).toInt()

        val OPT_MX_RANK = "rank"
        val OPT_NS_NAME = "name"

        val OPT_SOA_RNAME = "rname"
        val OPT_SOA_SERIAL = "serial"
        val OPT_SOA_REFRESH = "refresh"
        val OPT_SOA_RETRY = "retry"
        val OPT_SOA_EXPIRE = "expire"
        val OPT_SOA_MINIMUM = "minimum"

        val MX_REQUIRED_OPTIONS = arrayOf(OPT_MX_RANK)
        val NS_REQUIRED_OPTIONS = arrayOf(OPT_NS_NAME)
        val SOA_REQUIRED_OPTIONS = arrayOf(OPT_SOA_RNAME)
    }
}

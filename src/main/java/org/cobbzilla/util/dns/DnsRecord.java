package org.cobbzilla.util.dns;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.experimental.Accessors;
import org.cobbzilla.util.string.StringUtil;

import java.beans.Transient;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

@Accessors(chain=true)
public class DnsRecord extends DnsRecordBase {

    public static final int DEFAULT_TTL = (int) TimeUnit.HOURS.toSeconds(1);

    public static final String OPT_MX_RANK = "rank";
    public static final String OPT_NS_NAME = "name";

    public static final String OPT_SOA_RNAME = "rname";
    public static final String OPT_SOA_SERIAL = "serial";
    public static final String OPT_SOA_REFRESH = "refresh";
    public static final String OPT_SOA_RETRY = "retry";
    public static final String OPT_SOA_EXPIRE = "expire";
    public static final String OPT_SOA_MINIMUM = "minimum";

    public static final String[] MX_REQUIRED_OPTIONS = {OPT_MX_RANK};
    public static final String[] NS_REQUIRED_OPTIONS = {OPT_NS_NAME};
    public static final String[] SOA_REQUIRED_OPTIONS = {OPT_SOA_RNAME};

    private int ttl = DEFAULT_TTL;
    private Map<String, String> options;

    public DnsRecord setOption(String optName, String value) {
        if (options == null) options = new HashMap<>();
        options.put(optName, value);
        return this;
    }

    public String getOption(String optName) { return options == null ? null : options.get(optName); }

    public int getIntOption(String optName, int defaultValue) {
        try {
            return Integer.parseInt(options.get(optName));
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    @JsonIgnore public String[] getRequiredOptions () {
        switch (getType()) {
            case MX: return MX_REQUIRED_OPTIONS;
            case NS: return NS_REQUIRED_OPTIONS;
            case SOA: return SOA_REQUIRED_OPTIONS;
            default: return StringUtil.EMPTY_ARRAY;
        }
    }

    @JsonIgnore public boolean hasAllRequiredOptions () {
        for (String opt : getRequiredOptions()) {
            if (options == null || !options.containsKey(opt)) return false;
        }
        return true;
    }

    @Transient public String getOptions_string() {
        final StringBuilder b = new StringBuilder();
        if (options != null) {
            for (Map.Entry<String, String> e : options.entrySet()) {
                if (b.length() > 0) b.append(",");
                if (empty(e.getValue())) {
                    b.append(e.getKey()).append("=true");
                } else {
                    b.append(e.getKey()).append("=").append(e.getValue());
                }
            }
        }
        return b.toString();
    }

    public DnsRecord setOptions_string(String arg) {
        if (options == null) options = new HashMap<>();
        if (empty(arg)) return this;

        for (String kvPair : arg.split(",")) {
            int eqPos = kvPair.indexOf("=");
            if (eqPos == kvPair.length()) throw new IllegalArgumentException("Option cannot end in '=' character");
            if (eqPos == -1) {
                options.put(kvPair.trim(), "true");
            } else {
                options.put(kvPair.substring(0, eqPos).trim(), kvPair.substring(eqPos+1).trim());
            }
        }
        return this;
    }

    public java.lang.String toString() {
        return "DnsRecord(super=" + super.toString() + ", ttl=" + this.ttl + ", options=" + this.options + ")";
    }

    public int getTtl() {
        return this.ttl;
    }

    public Map<String, String> getOptions() {
        return this.options;
    }

    public DnsRecord setTtl(int ttl) {
        this.ttl = ttl;
        return this;
    }

    public DnsRecord setOptions(Map<String, String> options) {
        this.options = options;
        return this;
    }
}

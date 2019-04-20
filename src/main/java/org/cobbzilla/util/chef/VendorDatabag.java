package org.cobbzilla.util.chef;

import lombok.experimental.Accessors;
import org.cobbzilla.util.security.ShaUtil;

import java.util.ArrayList;
import java.util.List;

@Accessors(chain=true)
public class VendorDatabag {

    public static final VendorDatabag NULL = new VendorDatabag();

    private String service_key_endpoint;
    private String ssl_key_sha;
    private List<VendorDatabagSetting> settings = new ArrayList<>();

    public VendorDatabag addSetting (VendorDatabagSetting setting) { settings.add(setting); return this; }

    public VendorDatabagSetting getSetting(String path) {
        for (VendorDatabagSetting s : settings) {
            if (s.getPath().equals(path)) return s;
        }
        return null;
    }

    public boolean containsSetting (String path) { return getSetting(path) != null; }

    public boolean isDefault (String path, String value) {
        final VendorDatabagSetting setting = getSetting(path);
        if (setting == null) return false;

        final String shasum = setting.getShasum();
        return shasum != null && ShaUtil.sha256_hex(value).equals(shasum);

    }

    public String getService_key_endpoint() {
        return this.service_key_endpoint;
    }

    public String getSsl_key_sha() {
        return this.ssl_key_sha;
    }

    public List<VendorDatabagSetting> getSettings() {
        return this.settings;
    }

    public VendorDatabag setService_key_endpoint(String service_key_endpoint) {
        this.service_key_endpoint = service_key_endpoint;
        return this;
    }

    public VendorDatabag setSsl_key_sha(String ssl_key_sha) {
        this.ssl_key_sha = ssl_key_sha;
        return this;
    }

    public VendorDatabag setSettings(List<VendorDatabagSetting> settings) {
        this.settings = settings;
        return this;
    }
}

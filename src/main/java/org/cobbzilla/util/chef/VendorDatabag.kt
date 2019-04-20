package org.cobbzilla.util.chef

import lombok.experimental.Accessors
import org.cobbzilla.util.security.ShaUtil

import java.util.ArrayList

@Accessors(chain = true)
class VendorDatabag {

    private var service_key_endpoint: String? = null
    private var ssl_key_sha: String? = null
    private var settings: MutableList<VendorDatabagSetting> = ArrayList()

    fun addSetting(setting: VendorDatabagSetting): VendorDatabag {
        settings.add(setting)
        return this
    }

    fun getSetting(path: String): VendorDatabagSetting? {
        for (s in settings) {
            if (s.path == path) return s
        }
        return null
    }

    fun containsSetting(path: String): Boolean {
        return getSetting(path) != null
    }

    fun isDefault(path: String, value: String): Boolean {
        val setting = getSetting(path) ?: return false

        val shasum = setting.shasum
        return shasum != null && ShaUtil.sha256_hex(value) == shasum

    }

    fun getService_key_endpoint(): String? {
        return this.service_key_endpoint
    }

    fun getSsl_key_sha(): String? {
        return this.ssl_key_sha
    }

    fun getSettings(): List<VendorDatabagSetting> {
        return this.settings
    }

    fun setService_key_endpoint(service_key_endpoint: String): VendorDatabag {
        this.service_key_endpoint = service_key_endpoint
        return this
    }

    fun setSsl_key_sha(ssl_key_sha: String): VendorDatabag {
        this.ssl_key_sha = ssl_key_sha
        return this
    }

    fun setSettings(settings: MutableList<VendorDatabagSetting>): VendorDatabag {
        this.settings = settings
        return this
    }

    companion object {

        val NULL = VendorDatabag()
    }
}

package org.cobbzilla.util.string

import org.slf4j.Logger

import java.io.File
import java.util.Locale

import org.cobbzilla.util.daemon.ZillaRuntime.empty

object LocaleUtil {

    private val log = org.slf4j.LoggerFactory.getLogger(LocaleUtil::class.java)

    fun findLocaleFile(base: File, locale: String): File? {

        if (empty(locale)) return if (base.exists()) base else null

        val localeParts = locale.toLowerCase().replace("-", "_").split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val lang = localeParts[0]
        val region = if (localeParts.size > 1) localeParts[1] else null
        val variant = if (localeParts.size > 2) localeParts[2] else null

        var found: File?
        if (!empty(variant)) {
            found = findSpecificLocaleFile(base, lang + "_" + region + "_" + variant)
            if (found != null) return found
        }
        if (!empty(region)) {
            found = findSpecificLocaleFile(base, lang + "_" + region)
            if (found != null) return found
        }
        found = findSpecificLocaleFile(base, lang)
        if (found != null) return found

        return if (base.exists()) base else null
    }

    private fun findSpecificLocaleFile(base: File, locale: String): File? {
        val filename = base.name
        val lastDot = filename.lastIndexOf('.')
        val prefix: String
        val suffix: String
        if (lastDot != -1) {
            prefix = filename.substring(0, lastDot)
            suffix = filename.substring(lastDot)
        } else {
            prefix = filename
            suffix = ""
        }
        val localeFile = File(base.parent, prefix + "_" + locale + suffix)
        return if (localeFile.exists()) localeFile else null
    }

    fun fromString(localeString: String): Locale {
        val parts = if (empty(localeString)) StringUtil.EMPTY_ARRAY else localeString.split("[-_]+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        when (parts.size) {
            3 -> return Locale(parts[0], parts[1], parts[2])
            2 -> return Locale(parts[0], parts[1])
            1 -> return Locale(parts[0])
            0 -> return Locale.getDefault()
            else -> {
                log.warn("fromString: invalid locale string: $localeString")
                return Locale.getDefault()
            }
        }
    }
}

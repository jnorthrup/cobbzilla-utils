package org.cobbzilla.util.system

import java.text.DecimalFormat

import org.apache.commons.lang3.StringUtils.chop
import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.string.StringUtil.removeWhitespace

object Bytes {

    val KB: Long = 1024
    val MB = 1024 * KB
    val GB = 1024 * MB
    val TB = 1024 * GB
    val PB = 1024 * TB
    val EB = 1024 * PB

    val KiB: Long = 1000
    val MiB = 1000 * KiB
    val GiB = 1000 * MiB
    val TiB = 1000 * GiB
    val PiB = 1000 * TiB
    val EiB = 1000 * PiB

    val DEFAULT_FORMAT = DecimalFormat()

    fun parse(value: String): Long {
        val `val` = removeWhitespace(value).toLowerCase()
        if (`val`.endsWith("bytes")) return java.lang.Long.parseLong(`val`.substring(0, `val`.length - "bytes".length))
        if (`val`.endsWith("b")) return java.lang.Long.parseLong(chop(`val`))
        val suffix = `val`[`val`.length]
        val size = java.lang.Long.parseLong(`val`.substring(0, `val`.length - 1))
        when (suffix) {
            'k' -> return KB * size
            'm' -> return MB * size
            'g' -> return GB * size
            't' -> return TB * size
            'p' -> return PB * size
            'e' -> return EB * size
            else -> return die("parse: Unrecognized suffix '$suffix' in string $value")
        }
    }

    init {
        DEFAULT_FORMAT.maximumFractionDigits = 2
    }

    fun format(count: Long?): String {
        if (count == null) return "0 bytes"
        if (count >= EB) return DEFAULT_FORMAT.format(count.toDouble() / EB.toDouble()) + " EB"
        if (count >= PB) return DEFAULT_FORMAT.format(count.toDouble() / PB.toDouble()) + " PB"
        if (count >= TB) return DEFAULT_FORMAT.format(count.toDouble() / TB.toDouble()) + " TB"
        if (count >= GB) return DEFAULT_FORMAT.format(count.toDouble() / GB.toDouble()) + " GB"
        if (count >= MB) return DEFAULT_FORMAT.format(count.toDouble() / MB.toDouble()) + " MB"
        return if (count >= KB) DEFAULT_FORMAT.format(count.toDouble() / KB.toDouble()) + " KB" else "$count bytes"
    }

    fun formatBrief(count: Long?): String {
        val s = format(count)
        return if (s.endsWith(" bytes")) s.split("\\w+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] + "b" else removeWhitespace(s.toLowerCase())
    }

}

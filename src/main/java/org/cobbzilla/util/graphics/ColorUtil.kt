package org.cobbzilla.util.graphics

import org.apache.commons.lang3.RandomUtils

import java.awt.*

import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.daemon.ZillaRuntime.empty
import org.cobbzilla.util.string.StringUtil.getHexValue

object ColorUtil {

    val ANSI_RESET = "\\033[0m"

    @JvmOverloads
    fun parseRgb(colorString: String, defaultRgb: Int? = null): Int {
        try {
            if (empty(colorString)) return defaultRgb!!
            if (colorString.startsWith("0x")) return Integer.parseInt(colorString.substring(2), 16)
            return if (colorString.startsWith("#")) Integer.parseInt(colorString.substring(1), 16) else Integer.parseInt(colorString, 16)

        } catch (e: Exception) {
            return defaultRgb
                    ?: die("parseRgb: '' was unparseable and no default value provided: " + e.javaClass.simpleName + ": " + e.message, e)
        }

    }

    fun rgb2ansi(color: Int): Int {
        return rgb2ansi(Color(color))
    }

    fun rgb2ansi(c: Color): Int {
        return 16 + 36 * (c.red / 51) + 6 * (c.green / 51) + c.blue / 51
    }

    fun rgb2hex(color: Int): String {
        val c = Color(color)
        return (getHexValue(c.red.toByte())
                + getHexValue(c.green.toByte())
                + getHexValue(c.blue.toByte()))
    }

    fun randomColor(mode: ColorMode): Int {
        return randomColor(null, mode)
    }

    @JvmOverloads
    fun randomColor(usedColors: Collection<Int>? = null, mode: ColorMode = ColorMode.rgb): Int {
        var `val`: Int
        do {
            `val` = RandomUtils.nextInt(0x000000, 0xffffff)
        } while (usedColors != null && usedColors.contains(`val`))
        return if (mode == ColorMode.rgb) `val` else rgb2ansi(`val`)
    }

    @JvmOverloads
    fun ansiColor(fg: Int, bg: Int? = null): String {
        val b = StringBuilder()
        b.append("\\033[38;5;")
                .append(rgb2ansi(fg))
                .append(if (bg == null) "" else ";48;5;" + rgb2ansi(bg))
                .append("m")
        return b.toString()
    }
}

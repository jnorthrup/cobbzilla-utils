package org.cobbzilla.util.graphics;

import org.apache.commons.lang3.RandomUtils;

import java.awt.*;
import java.util.Collection;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;
import static org.cobbzilla.util.string.StringUtil.getHexValue;

public class ColorUtil {

    public static final String ANSI_RESET = "\\033[0m";

    public static int parseRgb(String colorString, int defaultRgb) {
        if (empty(colorString)) return defaultRgb;
        if (colorString.startsWith("0x")) return Integer.parseInt(colorString.substring(2), 16);
        if (colorString.startsWith("#")) return Integer.parseInt(colorString.substring(1), 16);
        return defaultRgb;
    }

    public static int rgb2ansi(int rgb) { return rgb2ansi(new Color(rgb)); }

    protected static int rgb2ansi(Color c) {
        return 16 + (36 * (c.getRed() / 51)) + (6 * (c.getGreen() / 51)) + c.getBlue() / 51;
    }

    public static int parseAnsi(String color, int defaultColor) {
        int rgb = parseRgb(color, defaultColor);
        try {
            return rgb2ansi(rgb);
        } catch (Exception e) {
            return defaultColor;
        }
    }

    public static String rgb_hex(int color) {
        final Color c = new Color(color);
        return "0x"
                +getHexValue((byte) c.getRed())
                +getHexValue((byte) c.getGreen())
                +getHexValue((byte) c.getBlue());
    }

    public static int randomRgbColor() { return randomRgbColor(null); }
    public static int randomAnsiColor() { return randomAnsiColor(null); }

    public static int randomRgbColor(Collection<Integer> usedColors) {
        int val;
        do {
            val = RandomUtils.nextInt(0x000000, 0xffffff);
        } while (usedColors != null && usedColors.contains(val));
        return val;
    }

    public static int randomAnsiColor(Collection<Integer> usedColors) {
        int val;
        do {
            val = 16 + RandomUtils.nextInt(1, 216);
        }
        while (usedColors != null && usedColors.contains(val));
        return val;
    }

}

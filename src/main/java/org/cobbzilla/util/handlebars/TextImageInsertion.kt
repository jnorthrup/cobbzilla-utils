package org.cobbzilla.util.handlebars

import com.fasterxml.jackson.annotation.JsonIgnore
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import lombok.experimental.Accessors

import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.util.ArrayList

import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.daemon.ZillaRuntime.empty
import org.cobbzilla.util.io.FileUtil.temp

@NoArgsConstructor
@Accessors(chain = true)
class TextImageInsertion : ImageInsertion {

    @Getter
    @Setter
    private var content: String? = null

    @Getter
    @Setter
    private var fontFamily = "Arial"
    @Getter
    @Setter
    private var fontStyle = "plain"
    @Getter
    @Setter
    private var fontColor = "000000"
    @Getter
    @Setter
    private var fontSize = 14
    @Getter
    @Setter
    private var alpha = 255
    @Getter
    @Setter
    private var maxWidth = -1
    @Getter
    @Setter
    private var widthPadding = 10
    @Getter
    @Setter
    private var lineSpacing = 4

    private val red: Int
        @JsonIgnore get() = (java.lang.Long.parseLong(fontColor, 16) and 0xff0000).toInt() shr 16
    private val green: Int
        @JsonIgnore get() = (java.lang.Long.parseLong(fontColor, 16) and 0x00ff00).toInt() shr 8
    private val blue: Int
        @JsonIgnore get() = (java.lang.Long.parseLong(fontColor, 16) and 0x0000ff).toInt()

    private val awtFontColor: Color
        @JsonIgnore get() = Color(red, green, blue, getAlpha())

    private val awtFontStyle: Int
        @JsonIgnore get() {
            when (fontStyle.toLowerCase()) {
                "plain" -> return Font.PLAIN
                "bold" -> return Font.BOLD
                "italic" -> return Font.ITALIC
                else -> return Font.PLAIN
            }
        }

    // adapted from: https://stackoverflow.com/a/18800845/1251543
    override val imageFile: File?
        get() {
            if (empty(getContent())) return null

            var g2d = graphics2D

            val txt = getParsedText(g2d)
            if (width == 0f) width = txt.width.toFloat()
            if (height == 0f) height = txt.height.toFloat()

            g2d.dispose()

            val img = BufferedImage(txt.width, txt.height, BufferedImage.TYPE_INT_ARGB)
            g2d = img.createGraphics()
            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY)
            g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE)
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
            g2d.font = font

            val fm = g2d.fontMetrics
            g2d.color = awtFontColor
            for (i in txt.lines.indices) {
                val line = txt.lines[i]
                val y = getLineY(fm, i)
                g2d.drawString(line, 0, y)
            }
            g2d.dispose()
            val temp = temp(".$format")
            try {
                ImageIO.write(img, format, temp)
                return temp
            } catch (e: IOException) {
                return die<File>("getImageStream: $e", e)
            }

        }

    protected val parsedText: ParsedText
        get() = getParsedText(graphics2D)

    private val graphics2D: Graphics2D
        get() {
            val img = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
            val g2d = img.createGraphics()
            val font = font
            g2d.font = font
            return g2d
        }

    private val font: Font
        get() = Font(getFontFamily(), awtFontStyle, getFontSize())

    fun capitalizeContent() {
        content = if (content == null) null else content!!.toUpperCase()
    }

    constructor(other: TextImageInsertion) : super(other) {}
    constructor(spec: String) : super(spec) {}

    override fun setField(key: String, value: String) {
        when (key) {
            "content" -> content = value
            "fontFamily" -> fontFamily = value
            "fontStyle" -> fontStyle = value
            "fontColor" -> fontColor = value
            "fontSize" -> fontSize = Integer.parseInt(value)
            "alpha" -> alpha = Integer.parseInt(value)
            "maxWidth" -> maxWidth = Integer.parseInt(value)
            "widthPadding" -> widthPadding = Integer.parseInt(value)
            "lineSpacing" -> lineSpacing = Integer.parseInt(value)
            else -> super.setField(key, value)
        }
    }

    protected fun getParsedText(g2d: Graphics2D): ParsedText {
        val fm = g2d.fontMetrics
        val txt = ParsedText()
        var widest = -1
        val inLines = getContent()!!.trim({ it <= ' ' }).split("\n".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        if (getMaxWidth() == -1) {
            for (inLine in inLines) {
                txt.lines.add(inLine)
                txt.width = fm.stringWidth(getContent()!!) + getWidthPadding()
                if (txt.width > widest) widest = txt.width
            }

        } else {
            for (inLine in inLines) {
                val words = inLine.split("\\s+".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                var b = StringBuilder()
                for (word in words) {
                    val stringWidth = fm.stringWidth("$b $word")
                    if (stringWidth + getWidthPadding() > getMaxWidth()) {
                        if (b.length == 0) die<Any>("getImageFile: word too long for maxWidth=$maxWidth: $word")
                        txt.lines.add(b.toString())
                        b = StringBuilder(word)
                    } else {
                        if (b.length > 0) b.append(" ")
                        b.append(word)
                        if (stringWidth > widest) widest = stringWidth
                    }
                }
                txt.lines.add(b.toString())
            }
        }
        txt.width = widest + getWidthPadding()
        txt.height = getLineY(fm, txt.lines.size)
        return txt
    }

    protected fun getLineY(fm: FontMetrics, i: Int): Int {
        return (i + 1) * (fm.ascent + getLineSpacing())
    }

    fun determineHeight(): Int {
        return parsedText.height
    }

    private inner class ParsedText {
        var lines: MutableList<String> = ArrayList()
        var width: Int = 0
        var height: Int = 0
    }

    companion object {

        val NO_TEXT_INSERTIONS = arrayOfNulls<TextImageInsertion>(0)
    }
}

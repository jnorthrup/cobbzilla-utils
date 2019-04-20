package org.cobbzilla.util.system

import lombok.experimental.Accessors
import org.apache.tools.ant.util.LineOrientedOutputStream

import java.io.IOException
import java.util.ArrayList
import java.util.regex.Pattern

@Accessors(chain = true)
class CommandProgressFilter : LineOrientedOutputStream() {

    var pctDone = 0
        private set
    var indicatorPos = 0
        private set
    var isClosed = false
        private set
    private var callback: CommandProgressCallback? = null

    private val indicators = ArrayList<CommandProgressIndicator>()

    fun getCallback(): CommandProgressCallback? {
        return this.callback
    }

    fun setCallback(callback: CommandProgressCallback): CommandProgressFilter {
        this.callback = callback
        return this
    }

    private inner class CommandProgressIndicator @java.beans.ConstructorProperties("percent", "pattern")
    constructor(private var percent: Int, private var pattern: Pattern?) {

        fun getPercent(): Int {
            return this.percent
        }

        fun getPattern(): Pattern? {
            return this.pattern
        }

        fun setPercent(percent: Int): CommandProgressIndicator {
            this.percent = percent
            return this
        }

        fun setPattern(pattern: Pattern): CommandProgressIndicator {
            this.pattern = pattern
            return this
        }
    }

    fun addIndicator(pattern: String, pct: Int): CommandProgressFilter {
        indicators.add(CommandProgressIndicator(pct, Pattern.compile(pattern)))
        return this
    }

    @Throws(IOException::class)
    override fun close() {
        isClosed = true
    }

    @Throws(IOException::class)
    override fun processLine(line: String) {
        for (i in indicatorPos until indicators.size) {
            val indicator = indicators[indicatorPos]
            if (indicator.getPattern()!!.matcher(line).find()) {
                pctDone = indicator.getPercent()
                indicatorPos++
                if (callback != null) callback!!.updateProgress(CommandProgressMarker(pctDone, indicator.getPattern(), line))
                return
            }
        }
    }

}

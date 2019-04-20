package org.cobbzilla.util.system

import lombok.experimental.Accessors

import java.util.regex.Pattern

@Accessors(chain = true)
class CommandProgressMarker {

    private var percent: Int = 0
    private var pattern: Pattern? = null
    private var line: String? = null

    @java.beans.ConstructorProperties("percent", "pattern", "line")
    constructor(percent: Int, pattern: Pattern, line: String) {
        this.percent = percent
        this.pattern = pattern
        this.line = line
    }

    constructor() {}

    fun getPercent(): Int {
        return this.percent
    }

    fun getPattern(): Pattern? {
        return this.pattern
    }

    fun getLine(): String? {
        return this.line
    }

    fun setPercent(percent: Int): CommandProgressMarker {
        this.percent = percent
        return this
    }

    fun setPattern(pattern: Pattern): CommandProgressMarker {
        this.pattern = pattern
        return this
    }

    fun setLine(line: String): CommandProgressMarker {
        this.line = line
        return this
    }
}

package org.cobbzilla.util.javascript

import lombok.extern.slf4j.Slf4j

import java.util.HashMap

import org.cobbzilla.util.io.StreamUtil.stream2string
import org.cobbzilla.util.string.StringUtil.getPackagePath

@Slf4j
class StandardJsEngine @JvmOverloads constructor(minEngines: Int = 1, maxEngines: Int = 1) : JsEngine(JsEngineConfig(minEngines, maxEngines, STANDARD_FUNCTIONS)) {

    fun round(value: String, script: String): String {
        val ctx = HashMap<String, Any>()
        ctx["x"] = value
        try {
            return evaluateInt(script, ctx).toString()
        } catch (e: Exception) {
            log.warn("round('$value', '$script', NOT rounding due to exception: $e")
            return value
        }

    }

    companion object {

        val STANDARD_FUNCTIONS = stream2string(getPackagePath(StandardJsEngine::class.java) + "/standard_js_lib.js")

        private val ESC_DOLLAR = "__ESCAPED_DOLLAR_SIGN__"
        fun replaceDollarSigns(`val`: String): String {
            return `val`.replace("'$", ESC_DOLLAR)
                    .replace("(\\$(\\d+(\\.\\d{2})?))".toRegex(), "($2 * 100)")
                    .replace(ESC_DOLLAR, "'$")
        }
    }
}

package org.cobbzilla.util.javascript

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import lombok.experimental.Accessors
import lombok.extern.slf4j.Slf4j

import javax.script.*
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.atomic.AtomicInteger

import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.daemon.ZillaRuntime.empty
import org.cobbzilla.util.json.JsonUtil.fromJsonOrDie

@Accessors(chain = true)
@Slf4j
open class JsEngine @JvmOverloads constructor(config: JsEngineConfig = JsEngineConfig(1, 1, null)) {

    private val scriptEngineManager = ScriptEngineManager()
    private val availableScriptEngines: MutableList<ScriptEngine>

    private val maxEngines: Int
    private val defaultScript: String?

    protected val nashorn: ScriptEngine
        get() = getNashorn(true)

    private val nashCounter: AtomicInteger
    private val inUse = AtomicInteger(0)
    fun getDefaultScript(): String {
        return if (empty(defaultScript)) "" else defaultScript
    }

    init {
        availableScriptEngines = ArrayList(config.minEngines)
        maxEngines = config.maxEngines
        defaultScript = config.defaultScript
        for (i in 0 until config.minEngines) {
            availableScriptEngines.add(getNashorn(false))
        }
        nashCounter = AtomicInteger(availableScriptEngines.size)
    }

    protected fun getNashorn(report: Boolean): ScriptEngine {
        val engine = scriptEngineManager.getEngineByName("nashorn")
        if (report) log.info("getNashorn: creating scripting engine #" + nashCounter.incrementAndGet() + " (" + availableScriptEngines.size + " available, " + inUse.get() + " in use)")
        return engine
    }

    fun <T> evaluate(code: String, context: Map<String, Any>): T? {
        var engine: ScriptEngine? = null
        val numEngines: Int
        synchronized(availableScriptEngines) {
            numEngines = availableScriptEngines.size
            if (numEngines > 0) {
                engine = availableScriptEngines.removeAt(0)
            }
        }
        if (engine == null) {
            if (numEngines >= maxEngines) return die<T>("evaluate(" + code + "): maxEngines (" + maxEngines + ") reached, no js engines available to execute, " + inUse.get() + " in use")
            engine = nashorn
        }
        inUse.incrementAndGet()

        try {
            val scriptContext = SimpleScriptContext()
            val bindings = SimpleBindings()
            for ((key, value) in context) {
                val wrappedOut: Any?
                var wrappedArray: Array<Any>? = null
                if (value == null) {
                    wrappedOut = null
                } else if (value is JsWrappable) {
                    wrappedOut = value.jsObject()
                } else if (value is ArrayNode) {
                    wrappedOut = fromJsonOrDie(value as JsonNode, Array<Any>::class.java)
                } else if (value is JsonNode) {
                    wrappedOut = fromJsonOrDie(value, Any::class.java)
                } else if (value.javaClass.isArray) {
                    wrappedArray = value as Array<Any>
                    wrappedOut = null
                } else {
                    wrappedOut = value
                }
                if (wrappedArray != null) {
                    bindings[key] = wrappedArray
                } else {
                    bindings[key] = wrappedOut
                }
            }
            scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE)
            try {
                val eval = engine!!.eval(getDefaultScript() + "\n" + code, scriptContext)
                return eval as T
            } catch (e: ScriptException) {
                throw IllegalStateException(e)
            }

        } finally {
            synchronized(availableScriptEngines) {
                availableScriptEngines.add(engine)
            }
            inUse.decrementAndGet()
        }
    }

    fun evaluateBoolean(code: String, ctx: Map<String, Any>): Boolean {
        val result = evaluate<Any>(code, ctx)
        return if (result == null) false else java.lang.Boolean.valueOf(result.toString().toLowerCase())
    }

    fun evaluateBoolean(code: String, ctx: Map<String, Any>, defaultValue: Boolean): Boolean {
        try {
            return evaluateBoolean(code, ctx)
        } catch (e: Exception) {
            log.debug("evaluateBoolean: returning $defaultValue due to exception:$e")
            return defaultValue
        }

    }

    fun evaluateInt(code: String, ctx: Map<String, Any>): Int? {
        val result = evaluate<Any>(code, ctx) ?: return null
        return (result as? Number)?.toInt() ?: Integer.parseInt(result.toString().trim { it <= ' ' })
    }

    fun evaluateLong(code: String, ctx: Map<String, Any>): Long? {
        val result = evaluate<Any>(code, ctx) ?: return null
        return (result as? Number)?.toLong() ?: java.lang.Long.parseLong(result.toString().trim { it <= ' ' })
    }

    fun evaluateLong(code: String, ctx: Map<String, Any>, defaultValue: Long?): Long? {
        try {
            return evaluateLong(code, ctx)
        } catch (e: Exception) {
            log.debug("evaluateLong: returning $defaultValue due to exception:$e")
            return defaultValue
        }

    }

    fun evaluateString(condition: String, ctx: Map<String, Any>): String? {
        val rval = evaluate<Any>(condition, ctx) ?: return null

        if (rval is String) return rval.toString()
        return if (rval is Number) {
            if (rval.toString().endsWith(".0")) "" + rval.toLong() else rval.toString()
        } else rval.toString()
    }

    fun functionOfX(value: String, script: String): String {
        val ctx = HashMap<String, Any>()
        ctx["x"] = value
        try {
            return evaluateInt(script, ctx).toString()
        } catch (e: Exception) {
            log.warn("functionOfX('$value', '$script', NOT applying due to exception: $e")
            return value
        }

    }
}

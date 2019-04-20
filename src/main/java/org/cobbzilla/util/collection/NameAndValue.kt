package org.cobbzilla.util.collection

import com.fasterxml.jackson.annotation.JsonIgnore
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import lombok.experimental.Accessors
import org.cobbzilla.util.javascript.JsEngine

import java.util.*

import org.cobbzilla.util.daemon.ZillaRuntime.empty

@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
class NameAndValue {

    @Getter
    @Setter
    val name: String? = null
    val hasName: Boolean
        @JsonIgnore get() = !empty(name)

    @Getter
    @Setter
    val value: String? = null
    val hasValue: Boolean
        @JsonIgnore get() = !empty(value)

    fun hasName(): Boolean {
        return !empty(name)
    }

    fun hasValue(): Boolean {
        return !empty(value)
    }

    override fun toString(): String {
        return "$name: $value"
    }

    companion object {

        val EMPTY_ARRAY = arrayOfNulls<NameAndValue>(0)

        fun map2list(map: Map<String, Any>): List<NameAndValue> {
            val list = ArrayList<NameAndValue>(map.size)
            for ((key, value) in map) {
                list.add(NameAndValue(key, value?.toString()))
            }
            return list
        }

        fun find(pairs: Array<NameAndValue>?, name: String): String? {
            if (pairs == null) return null
            for (pair in pairs) if (pair.name == name) return pair.value
            return null
        }

        @JvmOverloads
        fun evaluate(pairs: Array<NameAndValue>, context: Map<String, Any>, engine: JsEngine = JsEngine()): Array<NameAndValue>? {

            if (empty(context) || empty(pairs)) return pairs

            val results = arrayOfNulls<NameAndValue>(pairs.size)
            for (i in pairs.indices) {
                val isCode = pairs[i].hasValue && pairs[i].value!!.trim { it <= ' ' }.startsWith("@")
                if (isCode) {
                    results[i] = NameAndValue(pairs[i].name, engine.evaluateString(pairs[i].value!!.trim { it <= ' ' }.substring(1), context))
                } else {
                    results[i] = pairs[i]
                }
            }

            return results
        }

        fun toMap(attrs: Array<NameAndValue>): Map<String, String> {
            val map = HashMap<String, String>()
            if (!empty(attrs)) {
                for (attr in attrs) {
                    map[attr.name] = attr.value
                }
            }
            return map
        }
    }

}

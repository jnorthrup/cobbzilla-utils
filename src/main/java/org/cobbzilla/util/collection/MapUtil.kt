package org.cobbzilla.util.collection

import com.fasterxml.jackson.core.type.TypeReference

import java.util.*

object MapUtil {

    val JSON_STRING_OBJECT_MAP: TypeReference<HashMap<String, Any>> = object : TypeReference<HashMap<String, Any>>() {

    }
    val JSON_STRING_STRING_MAP: TypeReference<HashMap<String, String>> = object : TypeReference<HashMap<String, String>>() {

    }

    fun toMap(props: Properties?): Map<String, String> {
        if (props == null || props.isEmpty) return emptyMap()
        val map = LinkedHashMap<String, String>(props.size)
        for (name in props.stringPropertyNames()) map[name] = props.getProperty(name)
        return map
    }

    fun <K, V> deepEquals(m1: Map<K, V>?, m2: Map<K, V>?): Boolean {
        if (m1 == null) return m2 == null
        if (m2 == null) return false
        if (m1.size != m2.size) return false
        val set = m1.entries
        for ((key, m1v) in set) {
            val m2v = m2[key] ?: return false
            if (m1v is Map<*, *> && !deepEquals(m1v as Map<K, V>, m2v as Map<K, V>) || m1v != m2v) {
                return false
            }
        }
        return true
    }

    fun <K, V> deepHash(m: Map<K, V>): Int {
        var hash = 0
        for ((key, value) in m) {
            hash = 31 * hash + key.hashCode() + 31 * value.hashCode()
        }
        return hash
    }
}

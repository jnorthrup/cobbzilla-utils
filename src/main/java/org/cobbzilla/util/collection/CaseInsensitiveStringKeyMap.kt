package org.cobbzilla.util.collection

import java.util.Collections
import java.util.Enumeration
import java.util.concurrent.ConcurrentHashMap

open class CaseInsensitiveStringKeyMap<V> : ConcurrentHashMap<String, V>() {

    private val origKeys = ConcurrentHashMap<String, String>()

    fun key(key: Any?): String? {
        return key?.toString()?.toLowerCase()
    }

    override fun keySet(): ConcurrentHashMap.KeySetView<String, V> {
        return super.keys
    }

    override fun keys(): Enumeration<String> {
        return Collections.enumeration(origKeys.values)
    }

    override operator fun get(key: Any): V {
        return super.get(key(key)!!)
    }

    override fun containsKey(key: Any?): Boolean {
        return super.containsKey(key(key))
    }

    override fun put(key: String, value: V): V? {
        val ciKey = key(key)
        origKeys[ciKey!!] = key
        return super.put(ciKey, value)
    }

    override fun putIfAbsent(key: String, value: V): V? {
        val ciKey = key(key)
        (origKeys as java.util.Map<String, String>).putIfAbsent(ciKey, key)
        return (super as java.util.Map<String, V>).putIfAbsent(ciKey, value)
    }

    override fun remove(key: Any): V {
        val ciKey = key(key)
        origKeys.remove(ciKey!!)
        return super.remove(ciKey)
    }

    override fun remove(key: Any, value: Any?): Boolean {
        val ciKey = key(key)
        (origKeys as java.util.Map<String, String>).remove(ciKey, value)
        return (super as java.util.Map<String, V>).remove(ciKey, value)
    }

    override fun replace(key: String, oldValue: V, newValue: V): Boolean {
        val ciKey = key(key)
        return (super as java.util.Map<String, V>).replace(ciKey, oldValue, newValue)
    }

    override fun replace(key: String, value: V): V? {
        val ciKey = key(key)
        return super.replace(ciKey!!, value)
    }

}

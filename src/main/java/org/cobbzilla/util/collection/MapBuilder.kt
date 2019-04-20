package org.cobbzilla.util.collection

import java.util.HashMap
import java.util.LinkedHashMap

import org.cobbzilla.util.reflect.ReflectionUtil.instantiate

/**
 * A handy utility for creating and initializing Maps in a single statement.
 * @author Jonathan Cobb.
 */
object MapBuilder {

    /**
     * Most common create/init case. Usage:
     *
     * Map<String></String>, Boolean> myPremadeMap = MapBuilder.build(new Object[][]{
     * { "a", true }, { "b", false }, { "c", true }, { "d", true },
     * { "e", "yes, still dangerous but at least it's not an anonymous class" }
     * });
     *
     * If your keys and values are of the same type, it will even be typesafe:
     * Map<String></String>, String> someProperties = MapBuilder.build(new String[][]{
     * {"propA", "valueA" }, { "propB", "valueB" }
     * });
     *
     * @param values [x][2] array. items at [x][0] are keys and [x][1] are values.
     * @return a LinkedHashMap (to preserve order of declaration) with the "values" mappings
     */
    fun <K, V> build(values: Array<Array<Any>>): Map<K, V> {
        return build<K, V>(LinkedHashMap<Any, Any>() as Map<K, V>, values)
    }

    /**
     * Usage:
     * Map<K></K>,V> myMap = MapBuilder.build(new MyMapClass(options),
     * new Object[][]{ {k,v}, {k,v}, ... });
     * @param map add key/value pairs to this map
     * @return the map passed in, now containing new "values" mappings
     */
    fun <K, V> build(map: MutableMap<K, V>, values: Array<Array<Any>>): Map<K, V> {
        for (value in values) {
            map[value[0] as K] = value[1] as V
        }
        return map
    }

    /** Same as above, for single-value maps  */
    fun <K, V> build(map: MutableMap<K, V>, key: K, value: V): Map<K, V> {
        return build(map, arrayOf(arrayOf<Any>(key, value)))
    }

    /**
     * Usage:
     * Map<K></K>,V> myMap = MapBuilder.build(MyMapClass.class, new Object[][]{ {k,v}, {k,v}, ... });
     * @param mapClass a Class that implements Map
     * @return the map passed in, now containing new "values" mappings
     */
    fun <K, V> build(mapClass: Class<out Map<K, V>>, values: Array<Array<Any>>): Map<K, V> {
        return build<K, V>(instantiate<out Map<K, V>>(mapClass), values)
    }

    /** Usage: Map<K></K>,V> myMap = MapBuilder.build(key, value);  */
    fun <K, V> build(key: K, value: V): Map<K, V> {
        val map = HashMap<K, V>()
        map[key] = value
        return map
    }

}
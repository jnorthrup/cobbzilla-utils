package org.cobbzilla.util.collection.mappy

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import lombok.NoArgsConstructor

import java.util.Arrays.asList
import org.cobbzilla.util.json.JsonUtil.json
import org.cobbzilla.util.reflect.ReflectionUtil.arrayClass
import org.cobbzilla.util.reflect.ReflectionUtil.getFirstTypeParam

@NoArgsConstructor
class MappyStringKeyList<V> : MappyList<String, V> {

    constructor(size: Int) : super(size) {}

    constructor(size: Int, subSize: Int) : super(size, subSize) {}

    constructor(other: Map<String, Collection<V>>, subSize: Int?) : super(other) {
        this.subSize = subSize
    }

    constructor(other: Map<*, *>) : this(other, null) {}

    constructor(json: String) {
        val `object` = json(json, ObjectNode::class.java)
        val arrayClass = arrayClass<Any>(getFirstTypeParam<Any>(javaClass))
        val iter = `object`!!.fields()
        while (iter.hasNext()) {
            val entry = iter.next()
            putAll(entry.key, asList(*json<*>(entry.value, arrayClass) as Array<V>))
        }
    }

}

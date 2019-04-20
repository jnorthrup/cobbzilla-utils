package org.cobbzilla.util.json

import com.fasterxml.jackson.databind.JsonNode

import java.util.Comparator

import org.cobbzilla.util.json.JsonUtil.fromJsonOrDie

class JsonNodeComparator @java.beans.ConstructorProperties("path")
constructor(internal val path: String) : Comparator<JsonNode> {

    override fun compare(n1: JsonNode, n2: JsonNode): Int {
        val v1 = fromJsonOrDie(n1, path, JsonNode::class.java)
        val v2 = fromJsonOrDie(n2, path, JsonNode::class.java)
        if (v1 == null) return 1
        return if (v2 == null) -1 else v1.textValue().compareTo(v2.textValue())
    }
}

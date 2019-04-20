package org.cobbzilla.util.json

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.node.*
import org.cobbzilla.util.io.FileSuffixFilter
import org.cobbzilla.util.io.FileUtil
import org.cobbzilla.util.io.FilenameSuffixFilter
import org.cobbzilla.util.io.StreamUtil

import java.io.*
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import java.util.concurrent.ConcurrentHashMap

import org.cobbzilla.util.daemon.ZillaRuntime.big
import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.daemon.ZillaRuntime.empty

object JsonUtil {

    val EMPTY_JSON = "{}"
    val EMPTY_JSON_ARRAY = "[]"

    val MISSING: JsonNode = MissingNode.getInstance()

    val JSON_FILES: FileFilter = FileSuffixFilter(".json")
    val JSON_FILENAMES: FilenameFilter = FilenameSuffixFilter(".json")

    val FULL_MAPPER = ObjectMapper()
            .configure(SerializationFeature.INDENT_OUTPUT, true)

    val FULL_WRITER = FULL_MAPPER.writer()

    val FULL_MAPPER_ALLOW_COMMENTS = ObjectMapper()
            .configure(SerializationFeature.INDENT_OUTPUT, true)

    val FULL_MAPPER_ALLOW_COMMENTS_AND_UNKNOWN_FIELDS = ObjectMapper()
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    val FULL_MAPPER_ALLOW_UNKNOWN_FIELDS = ObjectMapper()
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    val NOTNULL_MAPPER = FULL_MAPPER
            .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)

    val NOTNULL_MAPPER_ALLOW_EMPTY = FULL_MAPPER
            .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)

    val PUBLIC_MAPPER = buildMapper()

    val PUBLIC_WRITER = buildWriter(PUBLIC_MAPPER, PublicView::class.java)

    private val viewWriters = ConcurrentHashMap<String, ObjectWriter>()

    init {
        FULL_MAPPER_ALLOW_COMMENTS.factory.enable(JsonParser.Feature.ALLOW_COMMENTS)
    }

    init {
        FULL_MAPPER_ALLOW_COMMENTS_AND_UNKNOWN_FIELDS.factory.enable(JsonParser.Feature.ALLOW_COMMENTS)
    }

    init {
        FULL_MAPPER_ALLOW_UNKNOWN_FIELDS.factory.enable(JsonParser.Feature.ALLOW_COMMENTS)
    }

    fun buildMapper(): ObjectMapper {
        return ObjectMapper()
                .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    fun buildWriter(view: Class<out PublicView>): ObjectWriter {
        return buildMapper().writerWithView(view)
    }

    fun buildWriter(mapper: ObjectMapper, view: Class<out PublicView>): ObjectWriter {
        return mapper.writerWithView(view)
    }

    fun newArrayNode(): ArrayNode {
        return ArrayNode(FULL_MAPPER.nodeFactory)
    }

    fun newObjectNode(): ObjectNode {
        return ObjectNode(FULL_MAPPER.nodeFactory)
    }

    fun find(array: JsonNode, name: String, value: String, returnValue: String): String? {
        if (array is ArrayNode) {
            for (i in 0 until array.size()) {
                val n = array.get(i).get(name)
                if (n != null && n.textValue() == value) {
                    val valNode = array.get(i).get(returnValue)
                    return valNode?.textValue()
                }
            }
        }
        return null
    }

    @JvmOverloads
    fun json_html(value: Any, m: ObjectMapper? = null): String {
        return (m?.let { json(value, it) }
                ?: json(value)).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace(" ", "&nbsp;").replace("\n", "<br/>")
    }

    class PublicView

    @Throws(Exception::class)
    @JvmOverloads
    fun toJson(o: Any?, m: ObjectMapper = NOTNULL_MAPPER): String {
        return m.writeValueAsString(o)
    }

    fun json(o: Any?): String {
        return toJsonOrDie(o)
    }

    fun json(o: Any, m: ObjectMapper): String {
        return toJsonOrDie(o, m)
    }

    fun toJsonOrDie(o: Any?): String {
        try {
            return toJson(o)
        } catch (e: Exception) {
            return die("toJson: exception writing object ($o): $e", e)
        }

    }

    fun toJsonOrDie(o: Any, m: ObjectMapper): String {
        try {
            return toJson(o, m)
        } catch (e: Exception) {
            return die("toJson: exception writing object ($o): $e", e)
        }

    }

    fun toJsonOrErr(o: Any): String {
        try {
            return toJson(o)
        } catch (e: Exception) {
            return e.toString()
        }

    }

    internal fun viewWriter(jsonView: Class<*>): ObjectWriter? {
        var w: ObjectWriter? = viewWriters[jsonView.name]
        if (w == null) {
            w = JsonUtil.NOTNULL_MAPPER.disable(MapperFeature.DEFAULT_VIEW_INCLUSION).writerWithView(jsonView)
            viewWriters[jsonView.name] = w
        }
        return w
    }

    @Throws(Exception::class)
    fun toJson(o: Any, jsonView: Class<*>): String {
        return viewWriter(jsonView)!!.writeValueAsString(o)
    }

    fun toJsonOrDie(o: Any, jsonView: Class<*>): String {
        try {
            return toJson(o, jsonView)
        } catch (e: Exception) {
            return die("toJson: exception writing object ($o): $e", e)
        }

    }

    fun toJsonOrErr(o: Any, jsonView: Class<*>): String {
        try {
            return toJson(o, jsonView)
        } catch (e: Exception) {
            return e.toString()
        }

    }

    @Throws(Exception::class)
    fun <T> fromJson(json: InputStream, clazz: Class<T>): T? {
        return fromJson(StreamUtil.toString(json), clazz)
    }

    @Throws(Exception::class)
    fun <T> fromJson(json: InputStream, clazz: Class<T>, mapper: ObjectMapper): T? {
        return fromJson(StreamUtil.toString(json), clazz, mapper)
    }

    @Throws(Exception::class)
    fun <T> fromJson(json: File, clazz: Class<T>): T? {
        return fromJson(FileUtil.toString(json), clazz)
    }

    @Throws(Exception::class)
    fun <T> fromJson(json: File, clazz: Class<T>, mapper: ObjectMapper): T? {
        return fromJson(FileUtil.toString(json), clazz, mapper)
    }

    @Throws(Exception::class)
    fun <T> fromJson(json: String?, clazz: Class<T>): T? {
        return fromJson(json, clazz, JsonUtil.FULL_MAPPER)
    }

    @Throws(Exception::class)
    fun <T> fromJson(json: String, type: JavaType): T? {
        return if (empty(json)) null else JsonUtil.FULL_MAPPER.readValue<T>(json, type)
    }

    @Throws(Exception::class)
    fun <T> fromJson(json: String?, clazz: Class<T>, mapper: ObjectMapper): T? {
        return if (empty(json)) null else mapper.readValue(json, clazz)
    }

    fun <T> fromJsonOrDie(json: File, clazz: Class<T>): T? {
        return fromJsonOrDie(FileUtil.toStringOrDie(json), clazz)
    }

    fun <T> json(json: String?, clazz: Class<T>): T? {
        return fromJsonOrDie(json, clazz)
    }

    fun <T> json(json: String, clazz: Class<T>, mapper: ObjectMapper): T? {
        return fromJsonOrDie(json, clazz, mapper)
    }

    fun <T> json(json: JsonNode, clazz: Class<T>): T {
        return fromJsonOrDie(json, clazz)
    }

    fun <T> json(json: Array<JsonNode>, clazz: Class<T>): List<T> {
        val list = ArrayList<T>()
        for (node in json) list.add(json(node, clazz))
        return list
    }

    fun <T> jsonWithComments(json: String, clazz: Class<T>): T? {
        return fromJsonOrDie(json, clazz, FULL_MAPPER_ALLOW_COMMENTS)
    }

    fun <T> jsonWithComments(json: JsonNode, clazz: Class<T>): T? {
        return fromJsonOrDie(json(json), clazz, FULL_MAPPER_ALLOW_COMMENTS)
    }

    fun <T> fromJsonOrDie(json: String?, clazz: Class<T>): T? {
        return fromJsonOrDie(json, clazz, FULL_MAPPER)
    }

    fun <T> fromJsonOrDie(json: String?, clazz: Class<T>, mapper: ObjectMapper): T? {
        if (empty(json)) return null
        try {
            return mapper.readValue(json, clazz)
        } catch (e: IOException) {
            return die<T>("fromJsonOrDie: exception while reading: $json: $e", e)
        }

    }

    @Throws(Exception::class)
    fun <T> fromJson(json: String, path: String, clazz: Class<T>): T {
        return fromJson(FULL_MAPPER.readTree(json), path, clazz)
    }

    @Throws(Exception::class)
    fun <T> fromJson(json: File, path: String, clazz: Class<T>): T {
        return fromJson(FULL_MAPPER.readTree(json), path, clazz)
    }

    @Throws(Exception::class)
    fun <T> fromJson(child: JsonNode, childClass: Class<out T>): T {
        return fromJson<out T>(child, "", childClass)
    }

    fun <T> fromJsonOrDie(child: JsonNode, childClass: Class<out T>): T {
        return fromJsonOrDie<out T>(child, "", childClass)
    }

    fun <T> fromJsonOrDie(node: JsonNode, path: String, clazz: Class<T>): T {
        return fromJsonOrDie(node, path, clazz, FULL_MAPPER)
    }

    @Throws(Exception::class)
    fun <T> fromJson(node: JsonNode, path: String, clazz: Class<T>): T {
        return fromJson(node, path, clazz, FULL_MAPPER)
    }

    fun <T> fromJsonOrDie(node: JsonNode, path: String, clazz: Class<T>, mapper: ObjectMapper): T {
        try {
            return fromJson(node, path, clazz, mapper)
        } catch (e: Exception) {
            return die("fromJsonOrDie: exception while reading: $node: $e", e)
        }

    }

    @Throws(Exception::class)
    fun <T> fromJson(node: JsonNode?, path: String, clazz: Class<T>, mapper: ObjectMapper): T {
        var node = node
        node = findNode(node, path)
        return mapper.convertValue(node, clazz)
    }

    @Throws(IOException::class)
    fun findNode(node: JsonNode?, path: String): JsonNode? {
        if (node == null) return null
        val nodePath = findNodePath(node, path)
        if (nodePath == null || nodePath.isEmpty()) return null
        val lastNode = nodePath[nodePath.size - 1]
        return if (lastNode === MISSING) null else lastNode
    }

    @Throws(JsonProcessingException::class)
    fun toString(node: Any?): String? {
        return if (node == null) null else FULL_MAPPER.writeValueAsString(node)
    }

    @Throws(IOException::class)
    fun nodeValue(node: JsonNode, path: String): String? {
        return fromJsonOrDie(toString(findNode(node, path)), String::class.java)
    }

    @Throws(IOException::class)
    fun findNodePath(node: JsonNode?, path: String): List<JsonNode>? {
        var node = node

        val nodePath = ArrayList<JsonNode>()
        nodePath.add(node)
        if (empty(path)) return nodePath
        val pathParts = tokenize(path)

        for (pathPart in pathParts) {
            var index = -1
            val bracketPos = pathPart.indexOf("[")
            val bracketClosePos = pathPart.indexOf("]")
            var isEmptyBrackets = false
            if (bracketPos != -1 && bracketClosePos != -1 && bracketClosePos > bracketPos) {
                if (bracketClosePos == bracketPos + 1) {
                    // ends with [], they mean to append
                    isEmptyBrackets = true
                } else {
                    index = Integer.parseInt(pathPart.substring(bracketPos + 1, bracketClosePos))
                }
                pathPart = pathPart.substring(0, bracketPos)
            }
            if (!empty(pathPart)) {
                node = node!!.get(pathPart)
                if (node == null) {
                    nodePath.add(MISSING)
                    return nodePath
                }
                nodePath.add(node)

            } else if (nodePath.size > 1) {
                return die<List<JsonNode>>("findNodePath: invalid path: $path")
            }
            if (index != -1) {
                node = node!!.get(index)
                nodePath.add(node)

            } else if (isEmptyBrackets) {
                nodePath.add(MISSING)
                return nodePath
            }
        }
        return nodePath
    }

    fun tokenize(path: String): List<String> {
        val pathParts = ArrayList<String>()
        val split = path.split("[\\.\\']+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()


        return Arrays.asList(*split)
    }

    @Throws(Exception::class)
    fun replaceNode(file: File, path: String, replacement: String): ObjectNode {
        return replaceNode(FULL_MAPPER.readTree(file) as ObjectNode, path, replacement)
    }

    @Throws(Exception::class)
    fun replaceNode(json: String, path: String, replacement: String): ObjectNode {
        return replaceNode(FULL_MAPPER.readTree(json) as ObjectNode, path, replacement)
    }

    @Throws(Exception::class)
    fun replaceNode(document: ObjectNode, path: String, replacement: String): ObjectNode {

        val simplePath = if (path.contains(".")) path.substring(path.lastIndexOf(".") + 1) else path
        var index: Int? = null
        if (simplePath.contains("[")) {
            index = Integer.parseInt(simplePath.substring(simplePath.indexOf("[") + 1, simplePath.indexOf("]")))
        }
        val found = findNodePath(document, path)
        if (found == null || found.isEmpty() || found[found.size - 1] == MISSING) {
            throw IllegalArgumentException("path not found: $path")
        }

        val parent = if (found.size > 1) found[found.size - 2] else document
        if (index != null) {
            val origNode = (parent as ArrayNode).get(index)
            parent.set(index, getValueNode(origNode, path, replacement))
        } else {
            // what is the original node type?
            val origNode = parent.get(simplePath)
            (parent as ObjectNode).set(simplePath, getValueNode(origNode, path, replacement))
        }
        return document
    }

    fun getValueNode(node: JsonNode, path: String, replacement: String): JsonNode {
        val nodeClass = node.javaClass.name
        if (node !is ValueNode) die<Any>("Path $path does not refer to a value (it is a $nodeClass)")
        if (node is TextNode) return TextNode(replacement)
        if (node is BooleanNode) return BooleanNode.valueOf(java.lang.Boolean.parseBoolean(replacement))
        if (node is IntNode) return IntNode(Integer.parseInt(replacement))
        if (node is LongNode) return LongNode(java.lang.Long.parseLong(replacement))
        if (node is DoubleNode) return DoubleNode(java.lang.Double.parseDouble(replacement))
        if (node is DecimalNode) return DecimalNode(big(replacement))
        return if (node is BigIntegerNode) BigIntegerNode(BigInteger(replacement)) else die("Path $path refers to an unsupported ValueNode: $nodeClass")
    }

    fun getNodeAsJava(node: JsonNode?, path: String): Any? {

        if (node == null || node is NullNode) return null
        val nodeClass = node.javaClass.name

        if (node is ArrayNode) {
            val array = arrayOfNulls<Any>(node.size())
            for (i in 0 until node.size()) {
                array[i] = getNodeAsJava(node.get(i), "$path[$i]")
            }
            return array
        }

        if (node is ObjectNode) {
            val map = HashMap<String, Any>(node.size())
            val iter = node.fieldNames()
            while (iter.hasNext()) {
                val name = iter.next()
                map[name] = getNodeAsJava(node.get(name), "$path.$name")
            }
            return map
        }

        if (node !is ValueNode) return node // return as-is...
        if (node is TextNode) return node.textValue()
        if (node is BooleanNode) return node.booleanValue()
        if (node is IntNode) return node.intValue()
        if (node is LongNode) return node.longValue()
        if (node is DoubleNode) return node.doubleValue()
        if (node is DecimalNode) return node.decimalValue()
        return if (node is BigIntegerNode) node.bigIntegerValue() else die<Any>("Path $path refers to an unsupported ValueNode: $nodeClass")
    }

    fun getValueNode(data: Any?): JsonNode {
        if (data == null) return NullNode.getInstance()
        if (data is Int) return IntNode((data as Int?)!!)
        if (data is Boolean) return BooleanNode.valueOf((data as Boolean?)!!)
        if (data is Long) return LongNode((data as Long?)!!)
        if (data is Float) return DoubleNode((data as Float?)!!.toDouble())
        if (data is Double) return DoubleNode((data as Double?)!!)
        if (data is BigDecimal) return DecimalNode(data as BigDecimal?)
        return if (data is BigInteger) BigIntegerNode(data as BigInteger?) else die("Cannot create value node from: " + data + " (type " + data.javaClass.name + ")")
    }

    fun toNode(f: File): JsonNode? {
        return fromJsonOrDie(FileUtil.toStringOrDie(f), JsonNode::class.java)
    }

    // adapted from: https://stackoverflow.com/a/11459962/1251543
    fun mergeNodes(mainNode: JsonNode?, updateNode: JsonNode): JsonNode? {

        val fieldNames = updateNode.fieldNames()
        while (fieldNames.hasNext()) {
            val fieldName = fieldNames.next()
            val jsonNode = mainNode!!.get(fieldName)
            // if field exists and is an embedded object
            if (jsonNode != null && jsonNode.isObject) {
                mergeNodes(jsonNode, updateNode.get(fieldName))
            } else {
                if (mainNode is ObjectNode) {
                    // Overwrite field
                    val value = updateNode.get(fieldName)
                    mainNode.set(fieldName, value)
                }
            }
        }

        return mainNode
    }

    fun mergeJsonOrDie(json: String, request: String): String {
        try {
            return mergeJson(json, request)
        } catch (e: Exception) {
            return die("mergeJsonOrDie: $e", e)
        }

    }

    @Throws(Exception::class)
    fun mergeJson(json: String, request: String): String {
        return mergeJson(json, fromJson(request, JsonNode::class.java))
    }

    @Throws(Exception::class)
    fun mergeJson(json: String, request: Any?): String {
        return json(mergeJsonNodes(json, request))
    }

    @Throws(Exception::class)
    fun mergeJsonNodes(json: String?, request: Any?): JsonNode? {
        if (request != null) {
            if (json != null) {
                val current = fromJson(json, JsonNode::class.java)
                val update: JsonNode
                if (request is JsonNode) {
                    update = request
                } else {
                    update = PUBLIC_MAPPER.valueToTree(request)
                }
                mergeNodes(current, update)
                return current
            } else {
                return PUBLIC_MAPPER.valueToTree(request)
            }
        }
        return json(json, JsonNode::class.java)
    }

    fun mergeJsonNodesOrDie(json: String, request: Any): JsonNode? {
        try {
            return mergeJsonNodes(json, request)
        } catch (e: Exception) {
            return die<JsonNode>("mergeJsonNodesOrDie: $e", e)
        }

    }

    fun mergeJsonOrDie(json: String, request: Any): String {
        try {
            return mergeJson(json, request)
        } catch (e: Exception) {
            return die("mergeJsonOrDie: $e", e)
        }

    }

}

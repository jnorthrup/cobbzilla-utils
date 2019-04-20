package org.cobbzilla.util.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.ValueNode
import lombok.experimental.Accessors

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.Reader
import java.net.URL
import java.util.*

import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.json.JsonUtil.*

/**
 * Facilitates editing JSON files.
 *
 * Notes:
 * - only one read operation can be specified. the 'edit' method will return the value of the first read operation processed.
 * - if you write a node that does not exist, it will be created (if it can be)
 */
@Accessors(chain = true)
class JsonEdit {

    private var jsonData: Any? = null
    private var operations: MutableList<JsonEditOperation> = ArrayList()

    fun addOperation(operation: JsonEditOperation): JsonEdit {
        operations.add(operation)
        return this
    }

    @Throws(Exception::class)
    fun edit(): String? {
        var root = readJson()
        for (operation in operations) {
            if (operation.isRead) return JsonUtil.toString(findNode(root, operation.path))
            root = apply(root, operation)
        }
        return JsonUtil.toString(JSON.treeToValue(root, Any::class.java))
    }

    @Throws(IOException::class)
    private fun readJson(): JsonNode {
        if (jsonData is JsonNode) return jsonData as JsonNode?
        if (jsonData is InputStream) return JSON.readTree(jsonData as InputStream?)
        if (jsonData is Reader) return JSON.readTree(jsonData as Reader?)
        if (jsonData is String) return JSON.readTree(jsonData as String?)
        if (jsonData is File) return JSON.readTree(jsonData as File?)
        if (jsonData is URL) return JSON.readTree(jsonData as URL?)
        throw IllegalArgumentException("jsonData is not a JsonNode, InputStream, Reader, String, File or URL")
    }

    @Throws(IOException::class)
    private fun apply(root: JsonNode, operation: JsonEditOperation): JsonNode {
        var root = root
        val path = findNodePath(root, operation.path)

        when (operation.type) {
            JsonEditOperationType.write -> root = write(root, path!!, operation)

            JsonEditOperationType.delete -> delete(path!!, operation)

            JsonEditOperationType.sort -> root = sort(root, path!!, operation)

            else -> throw IllegalArgumentException("unsupported operation: " + operation.type!!)
        }
        return root
    }

    @Throws(IOException::class)
    private fun write(root: JsonNode, path: List<JsonNode>, operation: JsonEditOperation): JsonNode {

        val current = path[path.size - 1]
        val parent = if (path.size > 1) path[path.size - 2] else null
        val data = operation.node

        if (current === MISSING) {
            // add a new node to parent
            return addToParent(root, operation, path)
        }

        if (current is ObjectNode) {
            if (data is ObjectNode) {
                val fields = data.fields()
                while (fields.hasNext()) {
                    val entry = fields.next()
                    current.set(entry.key, entry.value)
                }
            } else {
                return addToParent(root, operation, path)
            }

        } else if (current is ArrayNode) {
            if (operation.hasIndex()) {
                current.set(operation.index!!, operation.node)
            } else {
                current.add(operation.node)
            }

        } else if (current is ValueNode) {
            if (parent == null) return newObjectNode().set(operation.name, data)

            // overwrite value node at location
            addToParent(root, operation, path)

        } else {
            throw IllegalArgumentException("Cannot append to node (is a " + current.javaClass.name + "): " + current)
        }

        return root
    }

    @Throws(IOException::class)
    private fun addToParent(root: JsonNode, operation: JsonEditOperation, path: List<JsonNode>): JsonNode {
        var path = path

        var parent: JsonNode? = if (path.size > 1) path[path.size - 2] else null
        val data = operation.node

        if (parent == null) return newObjectNode().set(operation.name, data)

        if (parent is ObjectNode) {
            // more than one missing node?
            while (path.size <= operation.numPathSegments) {
                val childName = operation.getName(path.size - 2)
                val newNode = newObjectNode()
                (parent as ObjectNode).set(childName, newNode)

                // re-generate path now that we've created one missing parent
                path = findNodePath(root, operation.path)
                parent = newNode
            }

            // creating a new array node under parent?
            if (operation.isEmptyBrackets) {
                val newArrayNode = newArrayNode()
                (parent as ObjectNode).set(operation.name, newArrayNode)
                newArrayNode.add(data)

            } else {
                // otherwise, just set a field on the parent object
                (parent as ObjectNode).set(operation.name, data)
            }

        } else if (parent is ArrayNode) {
            if (operation.isEmptyBrackets) {
                parent.add(data)
            } else {
                parent.set(operation.index!!, data)
            }
        } else {
            throw IllegalArgumentException("Cannot append to node (is a " + parent.javaClass.name + "): " + parent)
        }

        return root
    }

    private fun delete(path: List<JsonNode>, operation: JsonEditOperation) {
        if (path.size < 2) throw IllegalArgumentException("Cannot delete root")
        val parent = path[path.size - 2]

        if (parent is ArrayNode) {
            parent.remove(operation.index!!)

        } else if (parent is ObjectNode) {
            parent.remove(operation.name)

        } else {
            throw IllegalArgumentException("Cannot remove node (parent is a " + parent.javaClass.name + ")")
        }
    }

    private fun sort(root: JsonNode, path: List<JsonNode>, operation: JsonEditOperation): JsonNode {
        val array = path[path.size - 1]
        val parent = if (path.size == 1) array else path[path.size - 2]
        if (!array.isArray) return die("sort: " + operation.path + " is not an array: " + json(array))

        // sort array
        val sorted = TreeSet(JsonNodeComparator(operation.json))
        val iter = array.iterator()
        while (iter.hasNext()) sorted.add(iter.next())

        // create a new ArrayNode for the sorted array
        val newArray = newArrayNode()
        for (n in sorted) newArray.add(n)

        // if we are sorting a root-level array, just return it now
        if (parent === array) return newArray

        // find previous array node, remove it and add new one
        var replaced = false
        if (parent.isArray) {
            val parentArray = parent as ArrayNode
            for (i in 0 until parentArray.size()) {
                if (parentArray.get(i) === array) {
                    parentArray.remove(i)
                    parentArray.insert(i, newArray)
                    replaced = true
                    break
                }
            }
        } else if (parent.isObject) {
            val parentObject = parent as ObjectNode
            val fieldNames = parentObject.fieldNames()
            while (fieldNames.hasNext()) {
                val fieldName = fieldNames.next()
                val n = parentObject.get(fieldName)
                if (n === array) {
                    parentObject.remove(fieldName)
                    parentObject.set(fieldName, newArray)
                    replaced = true
                    break
                }
            }
        }
        return if (!replaced) die("sort: error replacing original array with sorted array") else root
    }

    fun getJsonData(): Any? {
        return this.jsonData
    }

    fun getOperations(): List<JsonEditOperation> {
        return this.operations
    }

    fun setJsonData(jsonData: Any): JsonEdit {
        this.jsonData = jsonData
        return this
    }

    fun setOperations(operations: MutableList<JsonEditOperation>): JsonEdit {
        this.operations = operations
        return this
    }

    companion object {

        val JSON = FULL_MAPPER_ALLOW_COMMENTS
    }
}

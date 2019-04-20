package org.cobbzilla.util.json

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.JsonNode
import lombok.experimental.Accessors

import java.io.IOException

import org.cobbzilla.util.json.JsonUtil.FULL_MAPPER

@Accessors(chain = true)
class JsonEditOperation {

    private var type: JsonEditOperationType? = null
    private var path: String? = null
    private var json: String? = null

    val isRead: Boolean
        get() = type == JsonEditOperationType.read

    val node: JsonNode
        @Throws(IOException::class)
        get() = FULL_MAPPER.readTree(json)
    @JsonIgnore
    private var tokens: List<String>? = null

    val isEmptyBrackets: Boolean
        get() {
            val bracketPos = path!!.indexOf("[")
            val bracketClosePos = path!!.indexOf("]")
            return bracketPos != -1 && bracketClosePos != -1 && bracketClosePos == bracketPos + 1
        }

    val index: Int?
        get() {
            val tokens = getTokens()
            return if (tokens.size <= 1) index(path) else index(tokens[tokens.size - 1])
        }

    val name: String
        get() {
            val tokens = getTokens()
            return if (tokens.size <= 1) stripEmptyTrailingBrackets(path!!) else stripEmptyTrailingBrackets(tokens[tokens.size - 1])
        }

    val numPathSegments: Int
        get() = getTokens().size

    fun hasIndex(): Boolean {
        return index != null
    }

    private fun initTokens(): List<String> {
        return JsonUtil.tokenize(path!!)
    }

    private fun index(path: String?): Int? {
        try {
            val bracketPos = path!!.indexOf("[")
            val bracketClosePos = path.indexOf("]")
            if (bracketPos != -1 && bracketClosePos != -1 && bracketClosePos > bracketPos) {
                return Int(path.substring(bracketPos + 1, bracketClosePos))
            }
        } catch (ignored: Exception) {
        }

        return null
    }

    private fun stripEmptyTrailingBrackets(path: String): String {
        return if (path.endsWith("[]")) path.substring(0, path.length - 2) else path
    }

    fun getName(part: Int): String {
        return getTokens()[part]
    }

    fun getType(): JsonEditOperationType? {
        return this.type
    }

    fun getPath(): String? {
        return this.path
    }

    fun getJson(): String? {
        return this.json
    }

    fun getTokens(): List<String> {
        if (tokens == null) tokens = initTokens()
        return this.tokens
    }

    fun setType(type: JsonEditOperationType): JsonEditOperation {
        this.type = type
        return this
    }

    fun setPath(path: String): JsonEditOperation {
        this.path = path
        return this
    }

    fun setJson(json: String): JsonEditOperation {
        this.json = json
        return this
    }
}

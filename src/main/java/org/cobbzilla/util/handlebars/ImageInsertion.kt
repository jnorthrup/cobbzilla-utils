package org.cobbzilla.util.handlebars

import com.fasterxml.jackson.annotation.JsonIgnore
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import lombok.experimental.Accessors
import org.cobbzilla.util.string.StringUtil

import java.io.File
import java.io.IOException

import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.reflect.ReflectionUtil.copy

@NoArgsConstructor
@Accessors(chain = true)
abstract class ImageInsertion {

    @Getter
    @Setter
    private var name: String? = null
    @Getter
    @Setter
    private var page = 0
    @Getter
    @Setter
    private var x: Float = 0.toFloat()
    @Getter
    @Setter
    private var y: Float = 0.toFloat()
    @Getter
    @Setter
    private var width = 0f
    @Getter
    @Setter
    private var height = 0f
    @Getter
    @Setter
    private var format = "png"
    @Getter
    @Setter
    val isWatermark = false

    @get:JsonIgnore
    abstract val imageFile: File

    constructor(other: ImageInsertion) {
        copy(this, other)
    }

    constructor(spec: String) {
        for (part in StringUtil.split(spec, ", ")) {
            val eqPos = part.indexOf("=")
            if (eqPos == -1) die<Any>("invalid image insertion (missing '='): $spec")
            if (eqPos == part.length - 1) die<Any>("invalid image insertion (no value): $spec")
            val key = part.substring(0, eqPos).trim { it <= ' ' }
            val value = part.substring(eqPos + 1).trim { it <= ' ' }
            setField(key, value)
        }
    }

    fun init(map: Map<String, Any>) {
        for ((key, value) in map) {
            setField(key, value.toString())
        }
    }

    protected open fun setField(key: String, value: String) {
        when (key) {
            "name" -> this.name = value
            "page" -> this.page = Integer.parseInt(value)
            "x" -> this.x = java.lang.Float.parseFloat(value)
            "y" -> this.y = java.lang.Float.parseFloat(value)
            "width" -> this.width = java.lang.Float.parseFloat(value)
            "height" -> this.height = java.lang.Float.parseFloat(value)
            "format" -> this.format = value
            else -> die<Any>("invalid parameter: $key")
        }

    }
}

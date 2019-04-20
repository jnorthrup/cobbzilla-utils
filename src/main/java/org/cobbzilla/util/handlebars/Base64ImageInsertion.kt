package org.cobbzilla.util.handlebars

import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import lombok.experimental.Accessors
import org.apache.commons.codec.binary.Base64InputStream
import org.cobbzilla.util.io.FileUtil

import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException

import org.cobbzilla.util.daemon.ZillaRuntime.empty
import org.cobbzilla.util.io.FileUtil.temp

@NoArgsConstructor
@Accessors(chain = true)
class Base64ImageInsertion : ImageInsertion {

    @Getter
    @Setter
    private var image: String? = null // base64-encoded image data

    override val imageFile: File?
        @Throws(IOException::class)
        get() {
            if (empty(getImage())) return null
            val temp = temp(".$format")
            val stream = Base64InputStream(ByteArrayInputStream(image!!.toByteArray()))
            FileUtil.toFile(temp, stream)
            return temp
        }

    constructor(other: Base64ImageInsertion) : super(other) {}

    constructor(spec: String) : super(spec) {}

    override fun setField(key: String, value: String) {
        when (key) {
            "image" -> this.image = value
            else -> super.setField(key, value)
        }
    }

    companion object {

        val NO_IMAGE_INSERTIONS = arrayOfNulls<Base64ImageInsertion>(0)
    }

}

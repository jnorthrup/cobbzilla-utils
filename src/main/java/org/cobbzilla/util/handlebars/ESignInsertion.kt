package org.cobbzilla.util.handlebars

import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import lombok.experimental.Accessors

import java.io.File

import org.cobbzilla.util.daemon.ZillaRuntime.notSupported

@NoArgsConstructor
@Accessors(chain = true)
class ESignInsertion : ImageInsertion {

    @Getter
    @Setter
    private var role: String? = null

    override val imageFile: File
        get() = notSupported("getImageFile not supported for " + this.javaClass.name)

    constructor(other: ESignInsertion) : super(other) {}

    constructor(spec: String) : super(spec) {}

    override fun setField(key: String, value: String) {
        when (key) {
            "role" -> role = value
            else -> super.setField(key, value)
        }
    }

    companion object {

        val NO_ESIGN_INSERTIONS = arrayOfNulls<ESignInsertion>(0)
    }

}

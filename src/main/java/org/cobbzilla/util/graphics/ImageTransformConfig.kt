package org.cobbzilla.util.graphics

import org.cobbzilla.util.daemon.ZillaRuntime.die

class ImageTransformConfig(config: String) {

    var height: Int = 0
    var width: Int = 0

    init {
        val xpos = config.indexOf('x')
        try {
            width = Integer.parseInt(config.substring(xpos + 1))
            height = Integer.parseInt(config.substring(0, xpos))
        } catch (e: Exception) {
            die<Any>("invalid config (expected WxH): $config: $e", e)
        }

    }
}

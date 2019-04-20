package org.cobbzilla.util.xml

import org.cobbzilla.util.io.StreamUtil
import org.cobbzilla.util.string.StringUtil
import org.slf4j.Logger
import org.xml.sax.EntityResolver
import org.xml.sax.InputSource
import org.xml.sax.SAXException

import java.io.IOException
import java.util.HashMap

class CommonEntityResolver : EntityResolver {

    @Throws(SAXException::class, IOException::class)
    override fun resolveEntity(publicId: String, systemId: String): InputSource? {
        val resource = COMMON_ENTITY_MAP[publicId]
        if (resource == null) {
            val msg = "resolveEntity($publicId, $systemId) called, returning null"
            log.info(msg)
            println(msg)
            return null
        }
        return InputSource(StreamUtil.loadResourceAsStream(resource))
    }

    companion object {

        private val COMMON_DTD_ROOT = StringUtil.getPackagePath(CommonEntityResolver::class.java)
        private val COMMON_ENTITIES = arrayOf(arrayOf("-//W3C//DTD XHTML 1.0 Transitional//EN", "$COMMON_DTD_ROOT/xhtml1-transitional.dtd"), arrayOf("-//W3C//ENTITIES Latin 1 for XHTML//EN", "$COMMON_DTD_ROOT/xhtml-lat1.ent"), arrayOf("-//W3C//ENTITIES Symbols for XHTML//EN", "$COMMON_DTD_ROOT/xhtml-symbol.ent"), arrayOf("-//W3C//ENTITIES Special for XHTML//EN", "$COMMON_DTD_ROOT/xhtml-special.ent"))
        private val log = org.slf4j.LoggerFactory.getLogger(CommonEntityResolver::class.java)
        private val COMMON_ENTITY_MAP = HashMap<String, String>()

        init {
            for (entity in COMMON_ENTITIES) {
                COMMON_ENTITY_MAP[entity[0]] = entity[1]
            }
        }
    }

}

package org.cobbzilla.util.xml

import net.sf.saxon.Configuration
import net.sf.saxon.lib.NamespaceConstant
import net.sf.saxon.om.NodeInfo
import net.sf.saxon.om.TreeInfo
import net.sf.saxon.xpath.XPathFactoryImpl
import org.xml.sax.InputSource

import javax.xml.transform.sax.SAXSource
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathFactory
import java.io.ByteArrayInputStream
import java.util.HashMap
import java.util.concurrent.atomic.AtomicBoolean

import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.daemon.ZillaRuntime.empty

class XPath2(vararg expressions: String) {

    private val xpaths = HashMap<String, Path>()

    init {
        if (empty(expressions)) die<Any>("XPath2: no expressions")
        for (expr in expressions) {
            xpaths[expr] = Path(expr)
        }
    }

    fun firstMatches(xml: String): Map<String, String> {
        return firstMatches(Doc(xml))
    }

    fun firstMatches(doc: Doc): Map<String, String> {
        val matches = HashMap<String, String>()
        for ((key, value) in xpaths) {
            val match = value.firstMatch(doc)
            if (!empty(match)) matches[key] = match
        }
        return matches
    }

    fun firstMatch(xml: String): String? {
        when (xpaths.size) {
            0 -> return die<String>("firstMatch: no xpath expressions")
            1 -> {
                val matches = firstMatches(xml)
                return if (empty(matches)) null else matches.values.iterator().next()
            }
            else -> return die<String>("firstMatch: more than one xpath expression")
        }
    }

    fun getXpaths(): Map<String, Path> {
        return this.xpaths
    }

    class Path(xpath: String) {

        var expr: XPathExpression? = null
            private set

        init {
            try {
                expr = xPath.compile(xpath)
            } catch (e: Exception) {
                die<Any>("XPath2.Path: $e", e)
            }

        }

        fun firstMatch(xml: String): String? {
            return firstMatch(Doc(xml))
        }

        fun firstMatch(doc: Doc): String? {
            try {
                val matches = expr!!.evaluate(doc.doc, XPathConstants.NODESET) as List<*>
                if (empty(matches) || matches[0] == null) return null
                val line = matches[0] as NodeInfo
                return line.iterate().next().stringValue

            } catch (e: Exception) {
                return die<String>("firstMatch: $e", e)
            }

        }
    }

    class Doc(xml: String) {
        var doc: TreeInfo? = null
            private set

        init {
            val `is` = InputSource(ByteArrayInputStream(xml.toByteArray()))
            val ss = SAXSource(`is`)
            val config = (xpathFactory as XPathFactoryImpl).configuration
            try {
                doc = config.buildDocumentTree(ss)
            } catch (e: Exception) {
                die<Any>("XPath2.Doc: $e", e)
            }

        }
    }

    companion object {

        fun matchElement(element: String): String {
            return "//*[local-name()='$element']"
        }

        fun matchElements(elements: String): String {
            return matchElements(*elements.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        }

        fun matchElements(vararg elements: String): String {
            val b = StringBuilder()
            for (element in elements) {
                b.append("//*[local-name()='").append(element).append("']")
            }
            return b.toString()
        }

        fun xpath(element: String): XPath2 {
            return XPath2(matchElements(element))
        }

        fun xpath(vararg elements: String): XPath2 {
            return XPath2(matchElements(*elements))
        }

        val xpathFactory = initXpathFactory()
        private fun initXpathFactory(): XPathFactory {
            try {
                sysinit()
                return XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON)
            } catch (e: Exception) {
                return die("initXpathFactory: $e", e)
            }

        }

        val xPath = initXPath()
        private fun initXPath(): XPath {
            return xpathFactory.newXPath()
        }

        private val initialized = AtomicBoolean(false)
        private fun sysinit() {
            synchronized(initialized) {
                if (!initialized.get()) {
                    val name = "javax.xml.xpath.XPathFactory:" + NamespaceConstant.OBJECT_MODEL_SAXON
                    System.setProperty(name, "net.sf.saxon.xpath.XPathFactoryImpl")
                    initialized.set(true)
                }
            }
        }
    }
}

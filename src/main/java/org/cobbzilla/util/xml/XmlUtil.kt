package org.cobbzilla.util.xml

import org.atteo.xmlcombiner.XmlCombiner
import org.cobbzilla.util.string.StringUtil
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import java.io.*
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicReference
import java.util.regex.Pattern

import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.daemon.ZillaRuntime.empty
import org.cobbzilla.util.system.Bytes.KB

object XmlUtil {

    val XML10_UTF8 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
    val XML10_UTF8_REGEX = "(<\\?xml [^?]+\\?>)"
    val XML10_UTF8_PATTERN = Pattern.compile(XML10_UTF8_REGEX)

    fun merge(out: OutputStream, vararg documents: String): OutputStream {
        try {
            val combiner = XmlCombiner()
            for (document in documents) {
                combiner.combine(StringUtil.stream(document))
            }
            combiner.buildDocument(out)
            return out

        } catch (e: Exception) {
            return die("merge: $e", e)
        }

    }

    fun merge(vararg documents: String): String {
        return merge((32 * KB).toInt(), *documents)
    }

    fun merge(bufsiz: Int, vararg documents: String): String {
        val buffer = ByteArrayOutputStream(bufsiz)
        return merge(buffer, *documents).toString()
    }

    fun replaceElement(document: String, fromElement: String, toElement: String): String {
        return document
                .replace("<\\s*$fromElement([^>]*)>".toRegex(), "<$toElement$1>")
                .replace("</\\s*$fromElement\\s*>".toRegex(), "</$toElement>")
                .replace("<\\s*$fromElement\\s*/>".toRegex(), "<$toElement/>")
    }

    fun textElement(doc: Document, element: String, text: String): Element {
        val node = doc.createElement(element)
        node.appendChild(doc.createTextNode(text))
        return node
    }

    fun readDocument(xml: String): Document {
        try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val `is` = InputSource(StringReader(xml))
            return builder.parse(`is`)
        } catch (e: Exception) {
            return die("readDocument: $e", e)
        }

    }

    fun writeDocument(doc: Document, writer: Writer) {
        try {
            val tf = TransformerFactory.newInstance()
            val transformer = tf.newTransformer()
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
            transformer.transform(DOMSource(doc), StreamResult(writer))
        } catch (e: Exception) {
            die<Any>("writeDocument: $e", e)
        }

    }

    fun writeDocument(doc: Document): String {
        val writer = StringWriter()
        writeDocument(doc, writer)
        return writer.buffer.toString()
    }

    fun applyRecursively(element: Element, func: XmlElementFunction): Node {
        func.apply(element)
        val childNodes = element.childNodes
        if (childNodes != null && childNodes.length > 0) {
            for (i in 0 until childNodes.length) {
                val item = childNodes.item(i)
                if (item is Element) applyRecursively(item, func)
            }
        }
        return element
    }

    fun findElements(doc: Document, name: String): List<Element> {
        val found = ArrayList<Element>()
        applyRecursively(doc.documentElement, MatchNodeName(name, found))
        return found
    }

    fun findElements(element: Element, name: String): List<Element> {
        val found = ArrayList<Element>()
        applyRecursively(element, MatchNodeName(name, found))
        return found
    }

    fun findElements(element: Element, name: String, content: String?): List<Element> {
        if (content == null) return findElements(element, name)
        val found = ArrayList<Element>()
        applyRecursively(element, MatchNodeNameAndText(name, content, found))
        return found
    }

    fun findElements(element: Element, name: String, conditions: Map<String, String>): List<Element> {
        if (empty(conditions)) return findElements(element, name)
        val found = ArrayList<Element>()
        applyRecursively(element, MatchNodeSubElements(name, conditions, found))
        return found
    }

    fun findUniqueElement(doc: Document, name: String): Element? {
        val elements = findElements(doc, name)
        if (empty(elements)) return null
        return if (elements.size > 1) die<Element>("add: multiple $name elements found") else elements[0]
    }

    fun findFirstElement(doc: Document, name: String): Element? {
        val elements = findElements(doc, name)
        return if (empty(elements)) null else elements[0]
    }

    fun findFirstElement(e: Element, name: String): Element? {
        val elements = findElements(e, name)
        return if (empty(elements)) null else elements[0]
    }

    fun findFirstElement(e: Element, name: String, content: String): Element? {
        val elements = findElements(e, name, content)
        return if (empty(elements)) null else elements[0]
    }

    fun findFirstElement(e: Element, name: String, conditions: Map<String, String>): Element? {
        val elements = findElements(e, name, conditions)
        return if (empty(elements)) null else elements[0]
    }

    fun <T> findLargest(doc: Document, matcher: ElementMatcher, transformer: ElementTransformer<T>): T {
        val largest = AtomicReference<T>()
        applyRecursively(doc.documentElement, { element ->
            if (matcher.matches(element)) {
                val `val` = transformer.transform(element)
                if (`val` != null) {
                    synchronized(largest) {
                        val curVal = largest.get()
                        if (curVal == null || (`val` as Comparable<*>).compareTo(curVal) > 0) largest.set(`val`)
                    }
                }
            }
        })
        return largest.get()
    }

    fun removeElements(doc: Document, name: String) {
        for (e in XmlUtil.findElements(doc, name)) e.parentNode.removeChild(e)
    }

    fun same(n1: Node, n2: Node): Boolean {
        if (n1 === n2) return true
        if (n1.nodeName != n2.nodeName) return false
        val id1 = id(n1)
        val id2 = id(n2)
        return id1 != null && id2 != null && id1 == id2
    }

    fun id(n: Node): String? {
        return if (n.hasAttributes()) n.attributes.getNamedItem("id").textContent else null
    }

    fun getElementById(doc: Document, id: String): Element {
        val found = AtomicReference<Element>()
        applyRecursively(doc.documentElement, { element ->
            if (id == id(element)) {
                if (found.get() != null) die<Any>("multiple elements found with id=$id")
                found.set(element)
            }
        })
        return found.get()
    }

    fun stripXmlPreamble(xml: String): String {
        return XML10_UTF8_PATTERN.matcher(xml).replaceAll("")
    }

    private abstract class MatchNodeXMLFunction(protected val value: String?, protected val found: MutableList<Element>) : XmlElementFunction {

        init {
            if (value == null) die<Any>("Value cannot be null")
        }

        protected abstract fun check(element: Element): Boolean

        override fun apply(element: Element?) {
            if (element != null && check(element)) found.add(element)
        }
    }

    private open class MatchNodeName(value: String, found: MutableList<Element>) : MatchNodeXMLFunction(value, found) {

        override fun check(element: Element): Boolean {
            return value!!.equals(element.nodeName, ignoreCase = true)
        }
    }

    class MatchNodeNameAndText(name: String, text: String?, found: MutableList<Element>) : MatchNodeName(name, found) {
        private val text: String

        init {
            if (text == null) die<Any>("Text cannot be null")
            this.text = text!!.trim { it <= ' ' }
        }

        override fun check(element: Element): Boolean {
            return super.check(element) && text == element.textContent.trim { it <= ' ' }
        }
    }

    class MatchNodeSubElements(name: String, private val conditions: Map<String, String>, found: MutableList<Element>) : MatchNodeName(name, found) {

        init {
            if (empty(conditions)) die<Any>("Conditions map cannot be null")
        }

        override fun check(element: Element): Boolean {
            if (!super.check(element)) return false
            for ((key, value) in conditions) {
                if (findFirstElement(element, key, value) == null) return false
            }
            return true
        }
    }
}

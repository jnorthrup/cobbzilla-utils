package org.cobbzilla.util.xml

import org.apache.xpath.XPathAPI
import org.apache.xpath.objects.XObject
import org.slf4j.Logger
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.Text
import org.w3c.dom.traversal.NodeIterator
import org.xml.sax.InputSource
import org.xml.sax.SAXException

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.TransformerException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

import org.cobbzilla.util.daemon.ZillaRuntime.empty

class XPathUtil {

    var pathExpressions: Collection<String>? = null
    var isUseTidy = true

    constructor(expr: String) : this(arrayOf<String>(expr), true) {}
    constructor(expr: String, useTidy: Boolean) : this(arrayOf<String>(expr), useTidy) {}

    constructor(exprs: Array<String>) : this(Arrays.asList<String>(*exprs), true) {}
    constructor(exprs: Array<String>, useTidy: Boolean) : this(Arrays.asList<String>(*exprs), useTidy) {}

    constructor(passThruXPaths: Collection<String>) : this(passThruXPaths, true) {}

    constructor(exprs: Collection<String>, useTidy: Boolean) {
        this.pathExpressions = exprs
        this.isUseTidy = useTidy
    }

    constructor() {}

    @Throws(ParserConfigurationException::class, IOException::class, SAXException::class, TransformerException::class)
    fun getFirstMatchList(`in`: InputStream): List<Node> {
        return applyXPaths(`in`).values.iterator().next()
    }

    @Throws(ParserConfigurationException::class, IOException::class, SAXException::class, TransformerException::class)
    fun getFirstMatchList(xml: String): List<Node> {
        return applyXPaths(xml).values.iterator().next()
    }

    @Throws(ParserConfigurationException::class, IOException::class, SAXException::class, TransformerException::class)
    fun getFirstMatchMap(`in`: InputStream): Map<String, String> {
        val matchMap = applyXPaths(`in`)
        val firstMatches = HashMap<String, String>()
        for (key in matchMap.keys) {
            val found = matchMap[key]
            if (!found.isEmpty()) firstMatches[key] = found.get(0).textContent
        }
        return firstMatches
    }

    @Throws(ParserConfigurationException::class, IOException::class, SAXException::class, TransformerException::class)
    fun getFirstMatch(`in`: InputStream): Node? {
        val nodes = getFirstMatchList(`in`)
        return if (empty(nodes)) null else nodes[0]
    }

    @Throws(ParserConfigurationException::class, TransformerException::class, SAXException::class, IOException::class)
    fun getFirstMatchText(`in`: InputStream): String {
        return getFirstMatch(`in`)!!.textContent
    }

    @Throws(ParserConfigurationException::class, TransformerException::class, SAXException::class, IOException::class)
    fun getFirstMatchText(xml: String): String? {
        val match = getFirstMatch(ByteArrayInputStream(xml.toByteArray()))
        return match?.textContent
    }

    @Throws(IOException::class, SAXException::class, ParserConfigurationException::class, TransformerException::class)
    fun getStrings(`in`: InputStream): List<String> {
        val results = ArrayList<String>()
        val doc = getDocument(`in`)
        for (xpath in this.pathExpressions!!) {
            val found = XPathAPI.eval(doc, xpath)
            if (found != null) results.add(found.toString())
        }
        return results
    }

    @Throws(ParserConfigurationException::class, TransformerException::class, SAXException::class, IOException::class)
    fun applyXPaths(xml: String): Map<String, List<Node>> {
        return applyXPaths(ByteArrayInputStream(xml.toByteArray()))
    }

    @Throws(ParserConfigurationException::class, IOException::class, SAXException::class, TransformerException::class)
    fun applyXPaths(`in`: InputStream): Map<String, List<Node>> {
        val document = getDocument(`in`)
        return applyXPaths(document, document)
    }

    @Throws(TransformerException::class)
    fun applyXPaths(document: Document, node: Node): Map<String, List<Node>> {
        val allFound = HashMap<String, List<Node>>()
        // Use the simple XPath API to select a nodeIterator.
        // System.out.println("Querying DOM using "+pathExpression);
        for (xpath in this.pathExpressions!!) {
            val found = ArrayList<Node>()
            val nl = XPathAPI.selectNodeIterator(node, xpath)

            // Serialize the found nodes to System.out.
            // System.out.println("<output>");
            var n: Node
            while ((n = nl.nextNode()) != null) {
                if (isTextNode(n)) {
                    // DOM may have more than one node corresponding to a
                    // single XPath text node.  Coalesce all contiguous text nodes
                    // at this level
                    val sb = StringBuilder(n.nodeValue)
                    var nn = n.nextSibling
                    while (isTextNode(nn)) {
                        sb.append(nn.nodeValue)
                        nn = nn.nextSibling
                    }
                    val textNode = document.createTextNode(sb.toString())
                    found.add(textNode)

                } else {
                    found.add(n)
                    // serializer.transform(new DOMSource(n), new StreamResult(new OutputStreamWriter(System.out)));
                }
                // System.out.println();
            }
            // System.out.println("</output>");
            allFound[xpath] = found
        }
        return allFound
    }

    @Throws(ParserConfigurationException::class, SAXException::class, IOException::class)
    fun getDocument(xml: String): Document {
        return getDocument(ByteArrayInputStream(xml.toByteArray()))
    }

    @Throws(ParserConfigurationException::class, SAXException::class, IOException::class)
    fun getDocument(`in`: InputStream): Document {
        var inStream = `in`
        if (isUseTidy) {
            val out = ByteArrayOutputStream()
            TidyUtil.parse(`in`, out, true)
            inStream = ByteArrayInputStream(out.toByteArray())
        }

        val inputSource = InputSource(inStream)
        val dfactory = DocumentBuilderFactory.newInstance()
        dfactory.isNamespaceAware = false
        dfactory.isValidating = false
        // dfactory.setExpandEntityReferences(true);
        val documentBuilder = dfactory.newDocumentBuilder()
        documentBuilder.setEntityResolver(CommonEntityResolver())
        return documentBuilder.parse(inputSource)
    }

    companion object {

        val DOC_ROOT_XPATH = "/"
        private val log = org.slf4j.LoggerFactory.getLogger(XPathUtil::class.java)

        /** Decide if the node is text, and so must be handled specially  */
        fun isTextNode(n: Node?): Boolean {
            if (n == null) return false
            val nodeType = n.nodeType
            return nodeType == Node.CDATA_SECTION_NODE || nodeType == Node.TEXT_NODE
        }
    }
}

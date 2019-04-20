package org.cobbzilla.util.xml

import org.cobbzilla.util.collection.mappy.MappyList
import org.slf4j.Logger
import org.w3c.dom.Document
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList

import org.cobbzilla.util.handlebars.HandlebarsUtil.HB_END
import org.cobbzilla.util.handlebars.HandlebarsUtil.HB_START

class TidyHandlebarsSpanMerger : TidyHelper {

    override fun process(doc: Document) {
        val toRemove = MappyList<Node, Node>()
        mergeSpans(doc, toRemove)
        for ((key, value) in toRemove) {
            key.removeChild(value)
        }
    }

    protected fun mergeSpans(parent: Node, toRemove: MappyList<Node, Node>) {

        var spanStart: Node? = null
        var spanTemp: StringBuilder? = null
        val childNodes = parent.childNodes
        for (i in 0 until childNodes.length) {
            val child = childNodes.item(i)
            if (child.nodeType == Node.ELEMENT_NODE) {
                if (child.nodeName.equals("span", ignoreCase = true)) {
                    if (spanStart == null) {
                        spanStart = child
                        spanTemp = StringBuilder(collectText(child))

                    } else if (sameAttrs(spanStart.attributes, child.attributes)) {

                        append(spanTemp, collectText(child))
                        setSpan(spanStart, spanTemp)
                        toRemove[parent] = child
                    } else {
                        spanStart = child
                        spanTemp = StringBuilder(collectText(child))
                    }
                } else if (child.hasChildNodes()) {
                    mergeSpans(child, toRemove)
                }
                continue

            } else if (child.nodeType == Node.TEXT_NODE) {
                if (spanTemp != null) {
                    append(spanTemp, child.nodeValue)
                    setSpan(spanStart, spanTemp)
                    toRemove[parent] = child
                    continue
                }
            }
            if (child.hasChildNodes()) {
                mergeSpans(child, toRemove)
            }
        }
        if (spanStart != null && spanTemp != null && spanTemp.length > 0) {
            setSpan(spanStart, spanTemp)
        }
    }

    private fun setSpan(spanStart: Node?, spanTemp: StringBuilder?) {
        if (spanTemp == null || spanStart == null || spanStart.firstChild == null) return
        spanStart.firstChild.nodeValue = spanTemp.toString()
    }

    private fun append(b: StringBuilder, s: String?): StringBuilder {
        return if (s == null || s.length == 0) b else b.append(s)
    }

    private fun collectText(node: Node): String {
        val b = StringBuilder()
        if (node.hasChildNodes()) {
            val childNodes = node.childNodes
            for (i in 0 until childNodes.length) {
                val child = childNodes.item(i)
                if (child.nodeType == Node.TEXT_NODE) {
                    b.append(child.nodeValue)
                }
            }
        }
        return b.toString()
    }


    private fun sameAttrs(a1: NamedNodeMap, a2: NamedNodeMap): Boolean {
        if (isGoBack(a1) || isGoBack(a2)) return true
        if (a1.length != a2.length) return false
        for (i in 0 until a1.length) {
            var found = false
            val a1item = a1.item(i)
            for (j in 0 until a2.length) {
                if (a1item.nodeName.equals(a2.item(j).nodeName, ignoreCase = true) && a1item.nodeValue.equals(a2.item(j).nodeValue, ignoreCase = true)) {
                    found = true
                    break
                }
            }
            if (!found) return false
        }
        return true
    }

    private fun isGoBack(attrs: NamedNodeMap): Boolean {
        val id = attrs.getNamedItem("id")
        return id != null && id.nodeValue == "_GoBack"
    }

    companion object {

        val instance = TidyHandlebarsSpanMerger()
        private val log = org.slf4j.LoggerFactory.getLogger(TidyHandlebarsSpanMerger::class.java)

        fun scrubHandlebars(text: String): String {
            val b = StringBuilder()
            var start = 0
            var pos = text.indexOf(HB_START, start)
            while (pos != -1) {
                b.append(text.substring(start, pos)).append(HB_START)
                start = pos + 2
                val endPos = text.indexOf(HB_END, start)
                if (endPos == -1) {
                    b.append(text.substring(start))
                    return b.toString()
                } else {
                    b.append(scrubHtmlEntities(text.substring(start, endPos))).append(HB_END)
                    start = endPos + 2
                }
                pos = text.indexOf(HB_START, start)
            }
            if (start != text.length - 1) b.append(text.substring(start))
            return b.toString()
        }

        fun scrubHtmlEntities(s: String): String {
            return s.replace("&ldquo;", "\"").replace("&rdquo;", "\"")
                    .replace("&lsquo;", "'").replace("&rsquo;", "'")
        }
    }
}

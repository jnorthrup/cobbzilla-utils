package org.cobbzilla.util.xml

import org.w3c.dom.Document
import org.w3c.dom.Element

import java.util.concurrent.atomic.AtomicInteger

import org.cobbzilla.util.daemon.ZillaRuntime.empty

class ElementIdGenerator {

    private val prefix = ""
    private var counter: AtomicInteger? = null

    val idFunction = initIdFunction()

    @JvmOverloads
    constructor(start: Int = 1) {
        counter = AtomicInteger(start)
    }

    @JvmOverloads
    constructor(prefix: String, start: Int = 1) {
        this.prefix = prefix
        counter = AtomicInteger(start)
    }

    fun id(e: Element): Element {
        if (empty(e.getAttribute("id"))) e.setAttribute("id", prefix + counter!!.getAndIncrement())
        return e
    }

    fun create(doc: Document, elementName: String): Element {
        return id(doc.createElement(elementName))
    }

    @JvmOverloads
    fun text(doc: Document, elementName: String, text: String?, truncate: Int? = null): Element {
        var text = text
        val element = create(doc, elementName)

        if (text == null) return element
        text = text.trim { it <= ' ' }
        if (text.length == 0) return element

        if (truncate != null && text.trim { it <= ' ' }.length > truncate) text = text.trim { it <= ' ' }.substring(0, truncate)
        element.appendChild(doc.createTextNode(text))
        return element
    }

    private fun initIdFunction(): XmlElementFunction {
        return XmlElementFunction { this.id(it) }
    }
}

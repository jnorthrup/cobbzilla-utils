package org.cobbzilla.util.xml

import lombok.Cleanup
import org.cobbzilla.util.io.FileUtil
import org.w3c.dom.*
import org.w3c.tidy.Tidy

import java.io.*
import java.util.ArrayList
import java.util.HashSet

import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.daemon.ZillaRuntime.empty

object TidyUtil {

    @JvmOverloads
    fun tidy(file: File, vararg helpers: TidyHelper = null): String {
        return tidy(FileUtil.toStringOrDie(file), *helpers)
    }

    @JvmOverloads
    fun tidy(html: String?, vararg helpers: TidyHelper = null): String {
        try {
            @Cleanup val `in` = ByteArrayInputStream(html!!.toByteArray())
            @Cleanup val out = ByteArrayOutputStream()
            if (empty(helpers)) {
                parse(`in`, out, false)
            } else {
                val tidy = createTidy()
                val doc = tidy.parseDOM(`in`, null)
                for (helper in helpers) {
                    helper.process(doc)
                }
                tidy.pprint(doc, out)
            }
            return out.toString()

        } catch (e: Exception) {
            return die("tidy: $e", e)
        }

    }

    fun parse(`in`: InputStream, out: OutputStream, removeScripts: Boolean) {
        val tidy = createTidy()
        if (!removeScripts) {
            tidy.parse(`in`, out)
        } else {
            val doc = tidy.parseDOM(`in`, null)
            removeElement(doc.documentElement, "script")
            removeElement(doc.documentElement, "style")
            removeDuplicateAttributes(doc.documentElement)
            tidy.pprint(doc, out)
        }
    }

    fun removeDuplicateAttributes(parent: Node) {
        if (parent.nodeType == Node.ELEMENT_NODE) {
            val elt = parent as Element
            if (parent.getAttributes().length > 0) {
                val map = elt.attributes
                val found = HashSet<String>()
                var toRemove: MutableSet<Attr>? = null
                for (i in 0 until map.length) {
                    val attr = map.item(i) as Attr
                    if (found.contains(attr.nodeName)) {
                        if (toRemove == null) toRemove = HashSet()
                        toRemove.add(attr)
                    } else {
                        found.add(attr.nodeName)
                    }
                }
                if (toRemove != null) {
                    for (attr in toRemove) {
                        elt.removeAttributeNode(attr)
                    }
                }
            }
            val childNodes = elt.childNodes
            for (i in 0 until childNodes.length) {
                val child = childNodes.item(i)
                removeDuplicateAttributes(child)
            }
        }
    }

    fun removeElement(parent: Node, elementName: String?) {
        var toRemove: MutableList<Node>? = null
        var childNodes = parent.childNodes
        for (i in 0 until childNodes.length) {
            val child = childNodes.item(i)
            if (child.nodeType == Node.ELEMENT_NODE && child.nodeName.equals(elementName!!, ignoreCase = true)) {
                if (toRemove == null) toRemove = ArrayList()
                toRemove.add(child)
            }
        }
        if (toRemove != null) {
            for (dead in toRemove) {
                parent.removeChild(dead)
            }
        }
        childNodes = parent.childNodes
        for (i in 0 until childNodes.length) {
            removeElement(childNodes.item(i), elementName)
        }
    }

    fun createTidy(): Tidy {
        val tidy = Tidy()
        tidy.quiet = true
        // tidy.setMakeClean(true);
        // tidy.setIndentContent(true);
        tidy.smartIndent = true
        // tidy.setXmlOut(true);
        tidy.xhtml = true
        tidy.wraplen = Integer.MAX_VALUE

        // tidy.setDocType("omit");
        // tidy.setNumEntities(true);
        return tidy
    }
}

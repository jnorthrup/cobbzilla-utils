package org.cobbzilla.util.xml.main

import lombok.Cleanup
import org.cobbzilla.util.xml.XPathUtil
import org.w3c.dom.Node

import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import java.io.FileInputStream
import java.io.OutputStreamWriter

object XPath {

    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {

        val file = args[0]
        val expr = args[1]

        @Cleanup val `in` = FileInputStream(file)

        val serializer = TransformerFactory.newInstance().newTransformer()
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")

        val xp = XPathUtil(expr, true)
        val nodes = xp.applyXPaths(`in`)[expr]
        println("Found " + nodes.size + " matching nodes:")
        for (i in nodes.indices) {
            val n = nodes.get(i)
            print("match #$i: '")
            if (XPathUtil.isTextNode(n)) {
                print(n.textContent)
            } else {
                serializer.transform(DOMSource(n), StreamResult(OutputStreamWriter(System.out)))
            }
            println("' (end match #$i)")
        }
        println("DONE")

    }

}

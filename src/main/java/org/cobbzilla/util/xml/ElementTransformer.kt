package org.cobbzilla.util.xml

import org.w3c.dom.Element

interface ElementTransformer<T> {

    fun transform(e: Element): T

}

package org.cobbzilla.util.xml

import org.w3c.dom.Element

interface ElementMatcher {
    fun matches(e: Element): Boolean
}

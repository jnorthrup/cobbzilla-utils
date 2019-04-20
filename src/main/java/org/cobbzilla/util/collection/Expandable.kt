package org.cobbzilla.util.collection

interface Expandable<T> {

    fun expand(context: Map<String, Any>): List<T>

}

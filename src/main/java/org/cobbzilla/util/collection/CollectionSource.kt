package org.cobbzilla.util.collection

interface CollectionSource<T> {

    val values: Collection<T>

    fun addValue(`val`: T)
    fun addValues(vals: Collection<T>)

}

package org.cobbzilla.util.collection.mappy

import lombok.NoArgsConstructor

import java.util.ArrayList

@NoArgsConstructor
open class MappyList<K, V> : Mappy<K, V, List<V>> {

    protected var subSize: Int? = null

    constructor(size: Int) : super(size) {}
    constructor(size: Int, subSize: Int) : super(size) {
        this.subSize = subSize
    }

    @JvmOverloads
    constructor(other: Map<K, Collection<V>>, subSize: Int? = null) : super(other) {
        this.subSize = subSize
    }

    override fun newCollection(): List<V> {
        return if (subSize != null) ArrayList(subSize!!) else ArrayList()
    }

    override fun firstInCollection(collection: List<V>): V {
        return collection[0]
    }

}

package org.cobbzilla.util.collection.mappy

import lombok.NoArgsConstructor

import java.util.HashSet

@NoArgsConstructor
class MappySet<K, V> : Mappy<K, V, Set<V>> {

    constructor(size: Int) : super(size) {}

    override fun newCollection(): Set<V> {
        return HashSet()
    }

}


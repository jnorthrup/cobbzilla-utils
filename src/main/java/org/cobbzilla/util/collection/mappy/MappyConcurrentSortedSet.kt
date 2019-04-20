package org.cobbzilla.util.collection.mappy

import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter

import java.util.Comparator
import java.util.concurrent.ConcurrentSkipListSet

@NoArgsConstructor
@AllArgsConstructor
class MappyConcurrentSortedSet<K, V> : Mappy<K, V, ConcurrentSkipListSet<V>> {

    @Getter
    @Setter
    var comparator: Comparator<in V>? = null
        set(comparator) {
            field = this.comparator
        }

    constructor(size: Int) : super(size) {}

    override fun newCollection(): ConcurrentSkipListSet<V> {
        return if (this.comparator == null) ConcurrentSkipListSet() else ConcurrentSkipListSet(this.comparator)
    }

    override fun firstInCollection(collection: ConcurrentSkipListSet<V>): V {
        return collection.first()
    }

}

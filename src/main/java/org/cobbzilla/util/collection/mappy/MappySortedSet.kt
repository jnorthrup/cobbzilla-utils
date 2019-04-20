package org.cobbzilla.util.collection.mappy

import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter

import java.util.Comparator
import java.util.TreeSet

@NoArgsConstructor
@AllArgsConstructor
class MappySortedSet<K, V> : Mappy<K, V, TreeSet<V>> {

    @Getter
    @Setter
    var comparator: Comparator<in V>? = null
        set(comparator) {
            field = this.comparator
        }

    constructor(size: Int) : super(size) {}

    override fun newCollection(): TreeSet<V> {
        return if (this.comparator == null) TreeSet() else TreeSet(this.comparator)
    }

    override fun firstInCollection(collection: TreeSet<V>): V {
        return collection.first()
    }

}

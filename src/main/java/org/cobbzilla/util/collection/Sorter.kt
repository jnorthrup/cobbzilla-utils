package org.cobbzilla.util.collection

import java.util.Comparator
import java.util.TreeSet

object Sorter {

    fun <E> sort(things: Collection<E>, sorter: Comparator<*>): Collection<E> {
        return sort<TreeSet>(things, TreeSet(sorter))
    }

    fun <C : Collection<*>> sort(things: Collection<*>, rval: C): C {
        rval.addAll(things)
        return rval
    }

}

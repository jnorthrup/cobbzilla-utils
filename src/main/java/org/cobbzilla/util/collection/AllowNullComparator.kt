package org.cobbzilla.util.collection

import java.util.Comparator

import org.cobbzilla.util.daemon.ZillaRuntime.die

class AllowNullComparator<E> : Comparator<E> {

    override fun compare(o1: E?, o2: E?): Int {
        if (o1 == null) return if (o2 == null) 0 else -1
        if (o2 == null) return 1
        return if (o1 is Comparable<*> && o2 is Comparable<*>) (o1 as Comparable<*>).compareTo(o2) else die("compare: incomparable objects: $o1, $o2")
    }

    companion object {

        val STRING = AllowNullComparator<String>()
        val INT = AllowNullComparator<Int>()
        val LONG = AllowNullComparator<Long>()
    }

}

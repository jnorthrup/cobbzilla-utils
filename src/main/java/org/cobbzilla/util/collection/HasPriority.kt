package org.cobbzilla.util.collection

import java.util.Comparator

interface HasPriority {

    val priority: Int?

    fun hasPriority(): Boolean {
        return priority != null
    }

    companion object {

        val SORT_PRIORITY = { r1, r2 ->
            if (!r2.hasPriority()) return if (r1.hasPriority()) -1 else 0
            if (!r1.hasPriority()) return 1
            r1.priority!!.compareTo(r2.priority!!)
        }

        fun compare(o1: Any, o2: Any): Int {
            return if (o1 is HasPriority && o2 is HasPriority) SORT_PRIORITY.compare(o1, o2) else 0
        }
    }

}

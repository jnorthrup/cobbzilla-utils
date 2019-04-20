package org.cobbzilla.util.reflect

import java.util.Comparator

class FieldComparator<T, F : Comparable<F>> @java.beans.ConstructorProperties("field")
constructor(val field: String) : Comparator<T> {
    val isReverse = false

    override fun compare(o1: T, o2: T): Int {
        val v1 = ReflectionUtil.get(o1, field) as F
        val v2 = ReflectionUtil.get(o2, field) as F
        return if (isReverse)
            if (v1 == null) if (v2 == null) 0 else 1 else v2?.compareTo(v1) ?: 1
        else
            if (v1 == null) if (v2 == null) 0 else -1 else if (v2 == null) -1 else v1.compareTo(v2)
    }
}

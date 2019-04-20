package org.cobbzilla.util.collection

import java.math.BigDecimal
import java.util.Comparator

// adapted from: https://stackoverflow.com/a/2683388/1251543
class NumberComparator : Comparator<Number> {

    override fun compare(a: Number, b: Number): Int {
        return BigDecimal(a.toString()).compareTo(BigDecimal(b.toString()))
    }

    companion object {

        val INSTANCE = NumberComparator()
    }

}

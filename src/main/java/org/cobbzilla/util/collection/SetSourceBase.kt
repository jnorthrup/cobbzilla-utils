package org.cobbzilla.util.collection

import lombok.ToString
import org.cobbzilla.util.string.StringUtil
import java.util.HashSet
import java.util.concurrent.atomic.AtomicReference

open class SetSourceBase<T> : CollectionSource<T> {

    private val values = AtomicReference(HashSet<T>())

    override fun getValues(): Collection<T> {
        synchronized(values) {
            return HashSet(values.get())
        }
    }

    override fun addValue(`val`: T) {
        synchronized(values) {
            values.get().add(`val`)
        }
    }

    override fun addValues(vals: Collection<T>) {
        synchronized(values) {
            values.get().addAll(vals)
        }
    }

    override fun toString(): String {
        return StringUtil.toString(values.get(), ", ")
    }

}

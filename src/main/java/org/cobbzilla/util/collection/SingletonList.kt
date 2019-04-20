package org.cobbzilla.util.collection

import java.util.ArrayList

class SingletonList<E>(element: E) : ArrayList<E>() {

    init {
        super.add(element)
    }

    override operator fun set(index: Int, element: E?): E {
        throw unsupported()
    }

    override fun add(e: E?): Boolean {
        throw unsupported()
    }

    override fun add(index: Int, element: E?) {
        throw unsupported()
    }

    override fun remove(index: Int): E {
        throw unsupported()
    }

    override fun remove(o: Any?): Boolean {
        throw unsupported()
    }

    override fun clear() {
        throw unsupported()
    }

    override fun addAll(c: Collection<E>): Boolean {
        throw unsupported()
    }

    override fun addAll(index: Int, c: Collection<E>): Boolean {
        throw unsupported()
    }

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        throw unsupported()
    }

    override fun removeAll(c: Collection<*>): Boolean {
        throw unsupported()
    }

    override fun retainAll(c: Collection<*>): Boolean {
        throw unsupported()
    }

    private fun unsupported(): UnsupportedOperationException {
        return UnsupportedOperationException("singleton list is immutable")
    }

}

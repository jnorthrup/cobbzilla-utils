package org.cobbzilla.util.collection

import java.util.HashSet

import org.cobbzilla.util.daemon.ZillaRuntime.notSupported

class SingletonSet<E>(element: E) : HashSet<E>() {

    init {
        super.add(element)
    }

    override fun add(e: E?): Boolean {
        return notSupported()
    }

    override fun remove(o: Any?): Boolean {
        return notSupported()
    }

    override fun clear() {
        notSupported<Any>()
    }

    override fun addAll(c: Collection<E>): Boolean {
        return notSupported()
    }

    override fun retainAll(c: Collection<*>): Boolean {
        return notSupported()
    }

}

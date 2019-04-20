package org.cobbzilla.util.cache

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

import org.cobbzilla.util.daemon.ZillaRuntime.now

abstract class AutoRefreshingReference<T> {

    val `object` = AtomicReference<T>()
    val lastSet = AtomicLong()
    abstract val timeout: Long

    val isEmpty: Boolean
        get() = synchronized(`object`) {
            return `object`.get() == null
        }

    abstract fun refresh(): T

    fun get(): T {
        synchronized(`object`) {
            if (isEmpty || now() - lastSet.get() > timeout) update()
            return `object`.get()
        }
    }

    open fun update() {
        synchronized(`object`) {
            `object`.set(refresh())
            lastSet.set(now())
        }
    }

    fun flush() {
        set(null)
    }

    fun set(thing: T?) {
        synchronized(`object`) {
            `object`.set(thing)
            lastSet.set(now())
        }
    }
}

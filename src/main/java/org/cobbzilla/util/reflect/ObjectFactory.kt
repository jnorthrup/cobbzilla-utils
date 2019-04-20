package org.cobbzilla.util.reflect

interface ObjectFactory<T> {

    fun create(): T
    fun create(ctx: Map<String, Any>): T

}

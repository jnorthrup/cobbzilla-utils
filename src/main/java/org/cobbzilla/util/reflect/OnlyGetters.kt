package org.cobbzilla.util.reflect

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

import org.cobbzilla.util.daemon.ZillaRuntime.notSupported

object OnlyGetters {

    fun <T> wrap(thing: T): T {
        return Proxy.newProxyInstance(thing.javaClass.getClassLoader(), arrayOf(thing.javaClass), OnlyGettersInvocationHandler.instance!!) as T
    }

    private class OnlyGettersInvocationHandler : InvocationHandler {
        @Throws(Throwable::class)
        override fun invoke(proxy: Any, method: Method, args: Array<Any>?): Any {
            val name = method.name
            return if ((args == null || args.size == 0) && (name.startsWith("get") || name.startsWith("is") && method.returnType != Void::class.java)) {
                notSupported("immutable object: $proxy, cannot call $name")
            } else method.invoke(proxy, *args)
        }

        companion object {
            var instance: InvocationHandler? = null
        }
    }

}

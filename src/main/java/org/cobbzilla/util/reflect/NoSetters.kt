package org.cobbzilla.util.reflect

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

import org.cobbzilla.util.daemon.ZillaRuntime.notSupported

object NoSetters {

    fun <T> wrap(thing: T): T {
        return Proxy.newProxyInstance(thing.javaClass.getClassLoader(), arrayOf(thing.javaClass), NoSettersInvocationHandler.instance!!) as T
    }

    private class NoSettersInvocationHandler : InvocationHandler {
        @Throws(Throwable::class)
        override fun invoke(proxy: Any, method: Method, args: Array<Any>): Any {
            return if (method.name.startsWith("set")) notSupported("immutable object: " + proxy + ", cannot call " + method.name) else method.invoke(proxy, *args)
        }

        companion object {
            var instance: InvocationHandler? = null
        }
    }

}

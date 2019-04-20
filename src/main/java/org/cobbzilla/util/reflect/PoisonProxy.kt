package org.cobbzilla.util.reflect

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

import org.cobbzilla.util.daemon.ZillaRuntime.notSupported

object PoisonProxy {

    interface PoisonProxyThrow {
        fun getThrowable(proxy: Any, method: Method, args: Array<Any>): Throwable
    }

    fun <T> wrap(clazz: Class<T>): T {
        return wrap(clazz, null)
    }

    fun <T> wrap(clazz: Class<T>, thrower: PoisonProxyThrow?): T {
        return wrap(arrayOf<Class<*>>(clazz), thrower)
    }

    /**
     * Create a proxy object for a class where calling any methods on the object will result in it throwing an exception.
     * @param clazzes The classes to create a proxy for
     * @param thrower An object implementing the PoisonProxyThrow interface, which produces objects to throw
     * @param <T> The class to create a proxy for
     * @return A proxy to the class that will throw an exception if any methods are called on it
    </T> */
    fun <T> wrap(clazzes: Array<Class<*>>, thrower: PoisonProxyThrow?): T {
        return Proxy.newProxyInstance(clazzes[0].classLoader, clazzes, thrower?.let { PoisonedInvocationHandler(it) }
                ?: PoisonedInvocationHandler.instance) as T
    }

    private class PoisonedInvocationHandler : InvocationHandler {
        private val thrower: PoisonProxyThrow? = null

        @java.beans.ConstructorProperties("thrower")
        constructor(thrower: PoisonProxyThrow) {
            this.thrower = thrower
        }

        constructor() {}

        @Throws(Throwable::class)
        override fun invoke(proxy: Any, method: Method, args: Array<Any>): Any {
            return if (thrower == null) {
                notSupported("method not supported by poisonProxy: " + method.name + " (in fact, NO methods will work on this object)")
            } else {
                throw thrower.getThrowable(proxy, method, args)
            }
        }

        companion object {
            var instance = PoisonedInvocationHandler()
        }
    }

}

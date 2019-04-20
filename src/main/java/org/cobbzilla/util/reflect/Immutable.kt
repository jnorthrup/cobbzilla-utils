package org.cobbzilla.util.reflect

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

import org.cobbzilla.util.daemon.ZillaRuntime.die

class Immutable<T> @java.beans.ConstructorProperties("obj")
constructor(private val obj: T) : InvocationHandler {

    @Throws(Throwable::class)
    override fun invoke(proxy: Any, m: Method, args: Array<Any>): Any {

        val mName = m.name
        if (!(mName.startsWith("get")
                        || mName.startsWith("is")
                        || m.parameterTypes.size > 0
                        || Void::class.java.isAssignableFrom(m.returnType)))
            die<Any>("invoke(" + obj.javaClass.getSimpleName() + "." + mName + "): not a zero-arg getter or returns void: " + mName)

        return m.invoke(obj, *args)
    }

    companion object {

        fun <T> wrap(thing: T): T {
            val loader = thing.javaClass.getClassLoader()
            val classes = thing.javaClass.getInterfaces()
            return Proxy.newProxyInstance(loader, classes, Immutable(thing)) as T
        }
    }

}

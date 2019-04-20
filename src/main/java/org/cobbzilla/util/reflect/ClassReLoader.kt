package org.cobbzilla.util.reflect

import java.net.URLClassLoader
import java.util.HashSet

import java.lang.Class.forName

// adapted from https://stackoverflow.com/a/9192126/1251543
class ClassReLoader(toReload: Collection<String>) : URLClassLoader((ClassLoader.getSystemClassLoader() as URLClassLoader).urLs) {

    private val reload = HashSet<String>()
    fun doReloadFor(classOrPackage: String) {
        reload.add(classOrPackage)
    }

    init {
        reload.addAll(toReload)
    }

    @Throws(ClassNotFoundException::class)
    override fun loadClass(name: String): Class<*> {
        if (name.startsWith("java.")) return forName(name)
        if (!reload.contains(name)) {
            var find = name
            while (find.contains(".")) {
                find = find.substring(0, find.lastIndexOf("."))
                if (reload.contains(find)) {
                    return super.loadClass(name)
                }
            }
            return forName(name)
        }
        return super.loadClass(name)
    }
}

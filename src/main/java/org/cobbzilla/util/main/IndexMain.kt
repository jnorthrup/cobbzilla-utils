package org.cobbzilla.util.main

import com.google.common.collect.ImmutableMap
import org.cobbzilla.util.collection.ArrayUtil
import org.cobbzilla.util.io.main.FilesystemWatcherMain
import org.cobbzilla.util.io.main.JarTrimmerMain
import org.cobbzilla.util.json.main.JsonEditor
import org.cobbzilla.util.string.StringUtil
import org.slf4j.bridge.SLF4JBridgeHandler

import java.lang.reflect.Method

class IndexMain {
    fun getHandlers(): Map<String, Class<*>> {
        return handlers
    }

    companion object {

        val handlers: Map<String, Class<*>> = ImmutableMap.builder<String, Class<*>>()
                .put("json", JsonEditor::class.java)
                .put("fswatch", FilesystemWatcherMain::class.java)
                .put("trim-jar", JarTrimmerMain::class.java)
                .build()

        @JvmStatic
        fun main(args: Array<String>) {
            main(IndexMain::class.java, args)
        }

        protected fun main(clazz: Class<*>, args: Array<String>) {

            // redirect JUL -> logback using slf4j
            SLF4JBridgeHandler.removeHandlersForRootLogger()
            SLF4JBridgeHandler.install()

            if (args.size == 0) die("No command given. Use one of these: " + StringUtil.toString(handlers.keys, " "))

            // find the command
            val handler = handlers[args[0]]
            if (handler == null) die("Unrecognized command: " + args[0])

            // find the main method
            val mainMethod: Method?
            try {
                mainMethod = handler!!.getMethod("main", Array<String>::class.java)
            } catch (e: Exception) {
                die("Error loading main method: $e")
                return
            }

            if (mainMethod == null) die("No main method found for " + handler.name)

            // strip first arg (command name) and call main
            val newArgs = ArrayUtil.remove(args, 0)
            try {
                mainMethod!!.invoke(null, newArgs as Any)
            } catch (e: Exception) {
                die("Error running main method: $e")
            }

        }

        protected fun die(msg: String) {
            System.err.println(msg)
            System.exit(1)
        }
    }

}

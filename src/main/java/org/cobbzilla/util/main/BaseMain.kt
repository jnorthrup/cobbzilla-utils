package org.cobbzilla.util.main

import org.cobbzilla.util.daemon.ZillaRuntime
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.slf4j.Logger

import org.cobbzilla.util.daemon.ZillaRuntime.empty
import org.cobbzilla.util.reflect.ReflectionUtil.getFirstTypeParam
import org.cobbzilla.util.reflect.ReflectionUtil.instantiate

abstract class BaseMain<OPT : BaseMainOptions> {
    val options: OPT? = initOptions()

    protected val parser = CmdLineParser(options)

    var args: Array<String>? = null
        @Throws(CmdLineException::class)
        set(args) {
            field = args
            try {
                parser.parseArgument(*args)
                if (options.isHelp) {
                    showHelpAndExit()
                }
            } catch (e: Exception) {
                showHelpAndExit(e)
            }

        }

    protected fun initOptions(): OPT {
        return instantiate(getFirstTypeParam(javaClass))
    }

    @Throws(Exception::class)
    protected abstract fun run()

    fun runOrDie() {
        try {
            run()
        } catch (e: Exception) {
            die<Any>("runOrDie: $e", e)
        }

    }

    protected fun preRun() {}
    protected fun postRun() {}

    fun cleanup() {}

    fun showHelpAndExit() {
        parser.printUsage(System.out)
        System.exit(0)
    }

    fun showHelpAndExit(error: String) {
        showHelpAndExit(IllegalArgumentException(error))
    }

    fun showHelpAndExit(e: Exception) {
        parser.printUsage(System.err)
        if (e is CmdLineException && !empty(e.message)) {
            err(ERR_LINE + " >>> " + e.message + ERR_LINE)
        }
        System.exit(1)
    }

    fun <T> die(message: String): T? {
        if (options.isVerboseFatalErrors) {
            log.error(message)
        }
        err(message)
        System.exit(1)
        return null
    }

    fun <T> die(message: String, e: Exception): T? {
        if (options.isVerboseFatalErrors) {
            log.error(message, e)
        }
        err(message + ": " + e.javaClass.name + if (!empty(e.message)) ": " + e.message else "")
        System.exit(1)
        return null
    }

    companion object {

        private val log = org.slf4j.LoggerFactory.getLogger(BaseMain<*>::class.java)

        fun main(clazz: Class<out BaseMain<*>>, args: Array<String>) {
            var m: BaseMain<*>? = null
            var returnValue = 0
            try {
                m = clazz.newInstance()
                m!!.args = args
                m.preRun()
                m.run()
                m.postRun()

            } catch (e: Exception) {
                if (m == null || m.options == null || m.options!!.isVerboseFatalErrors) {
                    val msg = "Unexpected error: " + e + if (e.cause != null) " (caused by " + e.cause + ")" else ""
                    log.error(msg, e)
                    ZillaRuntime.die<Any>("Unexpected error: $e")
                } else {
                    val msg = e.javaClass.simpleName + if (e.message != null) ": " + e.message else ""
                    log.error(msg)
                }
                returnValue = -1
            } finally {
                m?.cleanup()
            }
            System.exit(returnValue)
        }

        val ERR_LINE = "\n--------------------------------------------------------------------------------\n"

        fun out(message: String) {
            println(message)
        }

        fun err(message: String) {
            System.err.println(message)
        }
    }
}

package org.cobbzilla.util.io.main

import org.cobbzilla.util.io.JarTrimmer
import org.cobbzilla.util.main.BaseMain

class JarTrimmerMain : BaseMain<JarTrimmerOptions>() {

    @Throws(Exception::class)
    override fun run() {
        val opts = options
        val trimmer = JarTrimmer()
        trimmer.trim(opts!!.config!!)
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            BaseMain.main(JarTrimmerMain::class.java, args)
        }
    }

}

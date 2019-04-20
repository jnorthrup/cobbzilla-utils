package org.cobbzilla.util.io.main

import org.cobbzilla.util.main.BaseMain

import org.cobbzilla.util.io.Decompressors.unroll
import org.cobbzilla.util.io.FileUtil.abs

class UnrollMain : BaseMain<UnrollOptions>() {

    @Throws(Exception::class)
    override fun run() {
        BaseMain.out(abs(unroll(options!!.file)))
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            BaseMain.main(UnrollMain::class.java, args)
        }
    }

}

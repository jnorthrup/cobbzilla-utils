package org.cobbzilla.util.http.main

import org.cobbzilla.util.http.HttpRequestBean
import org.cobbzilla.util.http.HttpResponseBean
import org.cobbzilla.util.http.HttpUtil
import org.cobbzilla.util.main.BaseMain

import org.cobbzilla.util.daemon.ZillaRuntime.readStdin
import org.cobbzilla.util.json.JsonUtil.json

class HttpMain : BaseMain<HttpMainOptions>() {

    @Throws(Exception::class)
    override fun run() {
        val request = json(readStdin(), HttpRequestBean::class.java)
        if (request == null) die<Any>("nothing read from stdin")
        val response = HttpUtil.getResponse(request!!)
        if (response.isOk) {
            BaseMain.out(response.entityString)
        } else {
            BaseMain.err(json(response.toMap()))
        }
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            BaseMain.main(HttpMain::class.java, args)
        }
    }

}

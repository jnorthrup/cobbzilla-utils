package org.cobbzilla.util.http

import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.phantomjs.PhantomJSDriver
import org.openqa.selenium.remote.DesiredCapabilities
import org.slf4j.Logger

import java.io.File

import org.cobbzilla.util.io.FileUtil.abs

open class PhantomUtil {

    private fun defaultDriver(): PhantomJSDriver {
        val capabilities = DesiredCapabilities()
        capabilities.isJavascriptEnabled = true
        return PhantomJSDriver(capabilities)
    }

    fun execJs(script: String): PhantomJSHandle {
        val driver = defaultDriver()
        val handle = PhantomJSHandle(driver)
        driver.errorHandler = handle
        driver.executePhantomJS(script)
        return handle
    }

    fun loadPage(file: File) {
        loadPageAndExec(file, "console.log('successfully loaded " + abs(file) + "')")
    }

    fun loadPage(url: String) {
        loadPageAndExec(url, "console.log('successfully loaded $url')")
    }

    fun loadPageAndExec(file: File, script: String) {
        loadPageAndExec("file://" + abs(file), script)
    }

    fun loadPageAndExec(url: String, script: String) {
        execJs(LOAD_AND_EXEC.replace("@@URL@@", url).replace("@@JS@@", script))
    }

    companion object {

        private val log = org.slf4j.LoggerFactory.getLogger(PhantomUtil::class.java)

        init {
            WebDriverManager.phantomjs().setup()
        }

        // ensures static-initializer above gets run
        fun init() {}

        val LOAD_AND_EXEC = "var page = require('webpage').create();\n" +
                "page.open('@@URL@@', function() {\n" +
                "  page.evaluateJavaScript('@@JS@@');\n" +
                "});\n"
    }
}

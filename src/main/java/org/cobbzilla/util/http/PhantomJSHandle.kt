package org.cobbzilla.util.http

import org.openqa.selenium.phantomjs.PhantomJSDriver
import org.openqa.selenium.remote.ErrorHandler
import org.slf4j.Logger

import java.io.Closeable

class PhantomJSHandle @java.beans.ConstructorProperties("driver")
constructor(val driver: PhantomJSDriver?) : ErrorHandler(), Closeable {

    override fun close() {
        if (driver != null) {
            driver.close()
            driver.quit()
        }
    }

    companion object {

        private val log = org.slf4j.LoggerFactory.getLogger(PhantomJSHandle::class.java)
    }
}
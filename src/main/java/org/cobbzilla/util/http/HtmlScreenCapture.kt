package org.cobbzilla.util.http

import lombok.Cleanup
import org.cobbzilla.util.io.StreamUtil
import org.slf4j.Logger

import java.io.File
import java.util.concurrent.TimeUnit

import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.daemon.ZillaRuntime.now
import org.cobbzilla.util.io.FileUtil.abs
import org.cobbzilla.util.string.StringUtil.getPackagePath
import org.cobbzilla.util.system.Sleep.sleep
import org.cobbzilla.util.time.TimeUtil.formatDuration

class HtmlScreenCapture : PhantomUtil() {

    @Synchronized
    fun capture(url: String, file: File) {
        capture(url, file, TIMEOUT)
    }

    @Synchronized
    fun capture(url: String, file: File, timeout: Long) {
        val script = SCRIPT.replace("@@URL@@", url).replace("@@FILE@@", abs(file))
        try {
            @Cleanup val handle = execJs(script)
            val start = now()
            while (file.length() == 0L && now() - start < timeout) sleep(200)
            if (file.length() == 0L && now() - start >= timeout) {
                sleep(5000)
                if (file.length() == 0L) die<Any>("capture: after " + formatDuration(timeout) + " file was never written to: " + abs(file) + ", handle=" + handle)
            }
        } catch (e: Exception) {
            die<Any>("capture: unexpected exception: $e", e)
        }

    }

    fun capture(`in`: File, out: File) {
        try {
            capture(`in`.toURI().toString(), out)
        } catch (e: Exception) {
            die<Any>("capture(" + abs(`in`) + "): " + e, e)
        }

    }

    companion object {

        private val TIMEOUT = TimeUnit.SECONDS.toMillis(60)

        val SCRIPT = StreamUtil.loadResourceAsStringOrDie(getPackagePath(HtmlScreenCapture::class.java) + "/html_screen_capture.js")
        private val log = org.slf4j.LoggerFactory.getLogger(HtmlScreenCapture::class.java)
    }

}

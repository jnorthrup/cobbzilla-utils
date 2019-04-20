package org.cobbzilla.util.system

import com.fasterxml.jackson.annotation.JsonIgnore
import lombok.experimental.Accessors
import org.slf4j.Logger

import java.io.ByteArrayOutputStream
import java.io.UnsupportedEncodingException

import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.string.StringUtil.UTF8

@Accessors(chain = true)
class CommandResult {

    private var stdout: String? = null
    private var stderr: String? = null

    private var exitStatus: Int? = null

    val isZeroExitStatus: Boolean
        @JsonIgnore get() = exitStatus != null && exitStatus == 0

    @JsonIgnore
    var exception: Exception? = null
        private set
    var exceptionString: String?
        get() = if (hasException()) exception!!.toString() else null
        set(ex) {
            exception = Exception(ex)
        }

    fun hasException(): Boolean {
        return exception != null
    }

    constructor(exitStatus: Int?, stdout: String, stderr: String) {
        this.exitStatus = exitStatus ?: -1
        this.stdout = stdout
        this.stderr = stderr
    }

    constructor(exitValue: Int, out: ByteArrayOutputStream?, err: ByteArrayOutputStream?) {
        this.exitStatus = exitValue
        try {
            this.stdout = out?.toString(UTF8)
            this.stderr = err?.toString(UTF8)
        } catch (e: UnsupportedEncodingException) {
            // should never happen
            die<Any>("CommandResult: couldn't convert stream to string: $e", e)
        }

    }

    constructor(e: Exception) {
        this.exception = e
    }

    constructor(e: Exception, out: ByteArrayOutputStream?, err: ByteArrayOutputStream?) {
        this.exception = e
        try {
            this.stdout = out?.toString(UTF8)
            this.stderr = err?.toString(UTF8)
        } catch (ex: UnsupportedEncodingException) {
            // should never happen
            log.warn("CommandResult: couldn't convert stream to string: $ex", ex)
        }

    }

    override fun toString(): String {
        return "{" +
                "exitStatus=" + exitStatus +
                ", stdout='" + stdout + '\''.toString() +
                ", stderr='" + stderr + '\''.toString() +
                ", exception=" + exceptionString +
                '}'.toString()
    }

    fun getStdout(): String? {
        return this.stdout
    }

    fun getStderr(): String? {
        return this.stderr
    }

    fun getExitStatus(): Int? {
        return this.exitStatus
    }

    fun setStdout(stdout: String): CommandResult {
        this.stdout = stdout
        return this
    }

    fun setStderr(stderr: String): CommandResult {
        this.stderr = stderr
        return this
    }

    fun setExitStatus(exitStatus: Int?): CommandResult {
        this.exitStatus = exitStatus
        return this
    }

    companion object {

        // useful for mocks
        val OK = CommandResult(0, null, null)
        private val log = org.slf4j.LoggerFactory.getLogger(CommandResult::class.java)
    }
}

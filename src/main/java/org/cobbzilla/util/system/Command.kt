package org.cobbzilla.util.system

import lombok.experimental.Accessors
import org.apache.commons.exec.CommandLine
import org.apache.commons.lang3.ArrayUtils
import org.cobbzilla.util.collection.SingletonList

import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.ArrayList

import org.cobbzilla.util.daemon.ZillaRuntime.empty
import org.cobbzilla.util.io.FileUtil.abs
import org.cobbzilla.util.string.StringUtil.UTF8cs

@Accessors(chain = true)
class Command {

    private var commandLine: CommandLine? = null
    private var input: String? = null
    private var rawInput: ByteArray? = null
    private var stdin: InputStream? = null
    private var dir: File? = null
    private var env: Map<String, String>? = null
    private var exitValues: MutableList<Int> = DEFAULT_EXIT_VALUES

    private var copyToStandard = false

    private var out: OutputStream? = null

    private var err: OutputStream? = null
    val inputStream: InputStream?
        get() {
            if (!hasInput()) return null
            if (stdin != null) return stdin
            return if (rawInput != null) ByteArrayInputStream(rawInput!!) else ByteArrayInputStream(input!!.toByteArray(UTF8cs))
        }

    constructor() {}

    fun hasOut(): Boolean {
        return out != null
    }

    fun hasErr(): Boolean {
        return err != null
    }

    constructor(commandLine: CommandLine) {
        this.commandLine = commandLine
    }

    constructor(command: String) : this(CommandLine.parse(command)) {}
    constructor(executable: File) : this(abs(executable)) {}

    fun hasDir(): Boolean {
        return dir != null
    }

    fun hasInput(): Boolean {
        return !empty(input) || !empty(rawInput) || stdin != null
    }

    fun getExitValues(): IntArray {
        return if (exitValues === DEFAULT_EXIT_VALUES)
            DEFAULT_EXIT_VALUES_INT
        else
            ArrayUtils.toPrimitive(exitValues.toTypedArray())
    }

    fun setExitValues(values: MutableList<Int>): Command {
        this.exitValues = values
        return this
    }

    fun setExitValues(values: IntArray): Command {
        exitValues = ArrayList(values.size)
        for (v in values) exitValues.add(v)
        return this
    }

    fun getCommandLine(): CommandLine? {
        return this.commandLine
    }

    fun getInput(): String? {
        return this.input
    }

    fun getRawInput(): ByteArray? {
        return this.rawInput
    }

    fun getStdin(): InputStream? {
        return this.stdin
    }

    fun getDir(): File? {
        return this.dir
    }

    fun getEnv(): Map<String, String>? {
        return this.env
    }

    fun isCopyToStandard(): Boolean {
        return this.copyToStandard
    }

    fun getOut(): OutputStream? {
        return this.out
    }

    fun getErr(): OutputStream? {
        return this.err
    }

    fun setCommandLine(commandLine: CommandLine): Command {
        this.commandLine = commandLine
        return this
    }

    fun setInput(input: String): Command {
        this.input = input
        return this
    }

    fun setRawInput(rawInput: ByteArray): Command {
        this.rawInput = rawInput
        return this
    }

    fun setStdin(stdin: InputStream): Command {
        this.stdin = stdin
        return this
    }

    fun setDir(dir: File): Command {
        this.dir = dir
        return this
    }

    fun setEnv(env: Map<String, String>): Command {
        this.env = env
        return this
    }

    fun setCopyToStandard(copyToStandard: Boolean): Command {
        this.copyToStandard = copyToStandard
        return this
    }

    fun setOut(out: OutputStream): Command {
        this.out = out
        return this
    }

    fun setErr(err: OutputStream): Command {
        this.err = err
        return this
    }

    companion object {

        val DEFAULT_EXIT_VALUES: MutableList<Int> = SingletonList(0)
        val DEFAULT_EXIT_VALUES_INT = intArrayOf(0)
    }
}

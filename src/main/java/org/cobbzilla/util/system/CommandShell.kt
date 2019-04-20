package org.cobbzilla.util.system

import lombok.Cleanup
import org.apache.commons.exec.*
import org.apache.commons.io.output.TeeOutputStream
import org.cobbzilla.util.collection.MapBuilder
import org.cobbzilla.util.io.FileUtil
import org.slf4j.Logger

import java.io.*
import java.util.*

import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.daemon.ZillaRuntime.empty
import org.cobbzilla.util.io.FileUtil.abs
import org.cobbzilla.util.io.FileUtil.getDefaultTempDir
import org.cobbzilla.util.string.StringUtil.ellipsis
import org.cobbzilla.util.string.StringUtil.trimQuotes

object CommandShell {

    internal val EXPORT_PREFIX = "export "

    val CHMOD = "chmod"
    val CHGRP = "chgrp"
    val CHOWN = "chown"

    private val DEFAULT_EXIT_VALUES = intArrayOf(0)
    private val log = org.slf4j.LoggerFactory.getLogger(CommandShell::class.java)
    val isRoot: Boolean
        get() = "root" == whoami()

    @Throws(IOException::class)
    fun loadShellExports(userFile: String): Map<String, String> {
        val file = userFile(userFile)
        if (!file.exists()) {
            throw IllegalArgumentException("file does not exist: " + abs(file))
        }
        return loadShellExports(file)
    }

    fun userFile(path: String): File {
        return File(System.getProperty("user.home") + File.separator + path)
    }

    @Throws(IOException::class)
    fun loadShellExports(f: File): Map<String, String> {
        FileInputStream(f).use { `in` -> return loadShellExports(`in`) }
    }

    @Throws(IOException::class)
    fun loadShellExports(`in`: InputStream): Map<String, String> {
        val map = HashMap<String, String>()
        BufferedReader(InputStreamReader(`in`)).use { reader ->
            var line: String
            var key: String
            var value: String?
            var eqPos: Int
            while ((line = reader.readLine()) != null) {
                line = line.trim { it <= ' ' }
                if (line.startsWith("#")) continue
                if (line.startsWith(EXPORT_PREFIX)) {
                    line = line.substring(EXPORT_PREFIX.length).trim { it <= ' ' }
                    eqPos = line.indexOf('=')
                    if (eqPos != -1) {
                        key = line.substring(0, eqPos).trim { it <= ' ' }
                        value = line.substring(eqPos + 1).trim { it <= ' ' }
                        value = trimQuotes(value)
                        map[key] = value
                    }
                }
            }
        }
        return map
    }

    fun loadShellExportsOrDie(f: String): Map<String, String> {
        try {
            return loadShellExports(f)
        } catch (e: Exception) {
            return die("loadShellExportsOrDie: $e", e)
        }

    }

    fun loadShellExportsOrDie(f: File): Map<String, String> {
        try {
            return loadShellExports(f)
        } catch (e: Exception) {
            return die("loadShellExportsOrDie: $e", e)
        }

    }

    @Throws(IOException::class)
    fun replaceShellExport(f: String, name: String, value: String) {
        replaceShellExports(File(f), MapBuilder.build(name, value))
    }

    @Throws(IOException::class)
    fun replaceShellExport(f: File, name: String, value: String) {
        replaceShellExports(f, MapBuilder.build(name, value))
    }

    @Throws(IOException::class)
    fun replaceShellExports(f: String, exports: Map<String, String>) {
        replaceShellExports(File(f), exports)
    }

    @Throws(IOException::class)
    fun replaceShellExports(f: File, exports: Map<String, String>) {

        // validate -- no quote chars allowed for security reasons
        for (key in exports.keys) {
            if (key.contains("\"") || key.contains("\'")) throw IllegalArgumentException("replaceShellExports: name cannot contain a quote character: $key")
            val value = exports[key]
            if (value.contains("\"") || value.contains("\'")) throw IllegalArgumentException("replaceShellExports: value for $key cannot contain a quote character: $value")
        }

        // read entire file as a string
        val contents = FileUtil.toString(f)

        // walk file line by line and look for replacements to make, overwrite file.
        val replaced = HashSet<String>(exports.size)
        FileWriter(f).use { w ->
            for (line in contents!!.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                line = line.trim { it <= ' ' }
                var found = false
                for (key in exports.keys) {
                    if (!line.startsWith("#") && line.matches("^\\s*export\\s+$key\\s*=.*".toRegex())) {
                        w.write("export " + key + "=\"" + exports[key] + "\"")
                        replaced.add(key)
                        found = true
                        break
                    }
                }
                if (!found) w.write(line)
                w.write("\n")
            }

            for (key in exports.keys) {
                if (!replaced.contains(key)) {
                    w.write("export " + key + "=\"" + exports[key] + "\"\n")
                }
            }
        }
    }

    @Throws(IOException::class)
    fun exec(commands: Collection<String>): MultiCommandResult {
        val result = MultiCommandResult()
        for (c in commands) {
            val command = Command(c)
            result.add(command, exec(c))
            if (result.hasException()) return result
        }
        return result
    }

    @Throws(IOException::class)
    fun exec(command: String): CommandResult {
        return exec(CommandLine.parse(command))
    }

    @Throws(IOException::class)
    fun exec(command: CommandLine): CommandResult {
        return exec(Command(command))
    }

    @Throws(IOException::class)
    fun exec(command: Command): CommandResult {

        val executor = DefaultExecutor()

        val outBuffer = ByteArrayOutputStream()
        var out = if (command.hasOut()) TeeOutputStream(outBuffer, command.out) else outBuffer
        if (command.isCopyToStandard) out = TeeOutputStream(out, System.out)

        val errBuffer = ByteArrayOutputStream()
        var err = if (command.hasErr()) TeeOutputStream(errBuffer, command.err) else errBuffer
        if (command.isCopyToStandard) err = TeeOutputStream(err, System.err)

        val handler = PumpStreamHandler(out, err, command.inputStream)
        executor.streamHandler = handler

        if (command.hasDir()) executor.workingDirectory = command.dir
        executor.setExitValues(command.exitValues)

        try {
            val exitValue = executor.execute(command.commandLine, command.env)
            return CommandResult(exitValue, outBuffer, errBuffer)

        } catch (e: Exception) {
            val stdout = outBuffer.toString().trim { it <= ' ' }
            val stderr = errBuffer.toString().trim { it <= ' ' }
            log.error("exec(" + command.commandLine + "): " + e
                    + (if (stdout.length > 0) "\nstdout=" + ellipsis(stdout, 1000)!! else "")
                    + if (stderr.length > 0) "\nstderr=" + ellipsis(stderr, 1000)!! else "")
            return CommandResult(e, outBuffer, errBuffer)
        }

    }

    @Throws(IOException::class)
    fun chmod(file: File, perms: String): Int {
        return chmod(abs(file), perms, false)
    }

    @Throws(IOException::class)
    fun chmod(file: File, perms: String, recursive: Boolean): Int {
        return chmod(abs(file), perms, recursive)
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun chmod(file: String, perms: String, recursive: Boolean = false): Int {
        val commandLine = CommandLine(CHMOD)
        if (recursive) commandLine.addArgument("-R")
        commandLine.addArgument(perms)
        commandLine.addArgument(abs(file), false)
        val executor = DefaultExecutor()
        return executor.execute(commandLine)
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun chgrp(group: String, path: File, recursive: Boolean = false): Int {
        return chgrp(group, abs(path), recursive)
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun chgrp(group: String, path: String, recursive: Boolean = false): Int {
        val executor = DefaultExecutor()
        val command = CommandLine(CHGRP)
        if (recursive) command.addArgument("-R")
        command.addArgument(group).addArgument(path)
        return executor.execute(command)
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun chown(owner: String, path: File, recursive: Boolean = false): Int {
        return chown(owner, abs(path), recursive)
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun chown(owner: String, path: String, recursive: Boolean = false): Int {
        val executor = DefaultExecutor()
        val command = CommandLine(CHOWN)
        if (recursive) command.addArgument("-R")
        command.addArgument(owner).addArgument(path)
        return executor.execute(command)
    }

    fun toString(command: String): String {
        try {
            return exec(command).stdout!!.trim { it <= ' ' }
        } catch (e: IOException) {
            return die("Error executing: $command: $e", e)
        }

    }

    fun hostname(): String {
        return toString("hostname")
    }

    fun domainname(): String {
        return toString("hostname -d")
    }

    fun whoami(): String {
        return toString("whoami")
    }

    fun locale(): String {
        return execScript("locale | grep LANG= | tr '=.' ' ' | awk '{print $2}'")!!.trim { it <= ' ' }
    }

    fun lang(): String {
        return execScript("locale | grep LANG= | tr '=_' ' ' | awk '{print $2}'")!!.trim { it <= ' ' }
    }

    fun tempScript(contents: String): File {
        var contents = contents
        contents = "#!/bin/bash\n\n$contents"
        try {
            val temp = File.createTempFile("tempScript", ".sh", getDefaultTempDir())
            FileUtil.toFile(temp, contents)
            chmod(temp, "700")
            return temp

        } catch (e: Exception) {
            return die("tempScript($contents) failed: $e", e)
        }

    }

    @JvmOverloads
    fun execScript(contents: String, env: Map<String, String>? = null, exitValues: MutableList<Int>? = null): String? {
        val result = scriptResult(contents, env, null, exitValues)
        if (!result.isZeroExitStatus && (exitValues == null || !exitValues.contains(result.exitStatus))) {
            die<Any>("execScript: non-zero exit: $result")
        }
        return result.stdout
    }

    fun scriptResult(contents: String, input: String): CommandResult {
        return scriptResult(contents, null, input, null)
    }

    @JvmOverloads
    fun scriptResult(contents: String, env: Map<String, String>? = null, input: String? = null, exitValues: MutableList<Int>? = null): CommandResult {
        try {
            @Cleanup("delete") val script = tempScript(contents)
            val command = Command(CommandLine(script)).setEnv(env).setInput(input)
            if (!empty(exitValues)) command.setExitValues(exitValues)
            return exec(command)
        } catch (e: Exception) {
            return die("Error executing: $e")
        }

    }

    fun okResult(result: CommandResult?): CommandResult? {
        if (result == null || !result.isZeroExitStatus) die<Any>("error: " + result!!)
        return result
    }

    fun home(user: String): File {
        val path = execScript("cd ~$user && pwd")
        if (empty(path)) die<Any>("home($user): no home found for user $user")
        val f = File(path!!)
        if (!f.exists()) die<Any>("home($user): home does not exist $path")
        return f
    }

    fun pwd(): File {
        return File(System.getProperty("user.dir"))
    }

}

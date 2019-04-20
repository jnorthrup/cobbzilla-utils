package org.cobbzilla.util.io.main

import lombok.Cleanup
import org.apache.commons.io.output.TeeOutputStream
import org.cobbzilla.util.daemon.AwaitResult
import org.cobbzilla.util.io.FilesystemWalker
import org.cobbzilla.util.io.UniqueFileFsWalker
import org.cobbzilla.util.main.BaseMain
import org.cobbzilla.util.string.StringUtil

import java.io.FileOutputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Writer

import java.util.stream.Collectors.toList

class UniqueFileWalkerMain : BaseMain<UniqueFileWalkerOptions>() {

    @Throws(Exception::class)
    override fun run() {
        val options = options

        val visitor = UniqueFileFsWalker(options!!.size)
        val result = FilesystemWalker()
                .setSize(options.size)
                .setThreads(options.threads)
                .withDirs(options.dirs)
                .withTimeoutDuration(options.timeoutDuration)
                .withVisitor(visitor)
                .walk()

        if (!result.allSucceeded()) {
            if (result.numFails() > 0) {
                BaseMain.out(">>>>> " + result.failures.values.size + " failures:")
                BaseMain.out(StringUtil.toString(result.failures.values, "\n-----"))
            }
            if (result.numTimeouts() > 0) {
                BaseMain.out(">>>>> " + result.timeouts.size + " timeouts")
            }
        }
        var i = 1
        val out: OutputStream
        if (options.hasOutfile()) {
            out = TeeOutputStream(FileOutputStream(options.outfile!!), System.out)
        } else {
            out = System.out
        }
        @Cleanup val w = OutputStreamWriter(out)
        for (dup in visitor.hash.values.stream().filter { v -> v.size > 1 }.collect<List<Set<String>>, Any>(toList())) {
            w.write("\n----- dup#" + i++ + ": \n")
            w.write(StringUtil.toString(dup, "\n"))
        }
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            BaseMain.main(UniqueFileWalkerMain::class.java, args)
        }
    }

}

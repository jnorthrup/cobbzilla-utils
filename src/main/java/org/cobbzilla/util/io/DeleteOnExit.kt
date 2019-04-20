package org.cobbzilla.util.io

import org.apache.commons.io.FileUtils
import org.slf4j.Logger

import java.io.File
import java.io.IOException
import java.util.ArrayList

class DeleteOnExit private constructor() : Runnable {

    override fun run() {
        for (path in paths) {
            if (!path.exists()) return
            if (path.isDirectory) {
                try {
                    FileUtils.deleteDirectory(path)
                } catch (e: IOException) {
                    log.warn("FileUtil.deleteOnExit: error deleting path=$path: $e", e)
                }

            } else {
                if (!path.delete()) {
                    log.warn("FileUtil.deleteOnExit: error deleting path=$path")
                }
            }
        }
    }

    companion object {

        private val log = org.slf4j.LoggerFactory.getLogger(DeleteOnExit::class.java)
        private val paths = ArrayList<File>()

        init {
            Runtime.getRuntime().addShutdownHook(Thread(DeleteOnExit()))
        }

        fun add(path: File) {
            paths.add(path)
        }
    }

}

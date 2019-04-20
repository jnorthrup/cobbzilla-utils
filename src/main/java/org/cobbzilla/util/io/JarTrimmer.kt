package org.cobbzilla.util.io

import lombok.Cleanup
import org.apache.commons.io.IOUtils
import org.cobbzilla.util.reflect.ReflectionUtil
import org.slf4j.Logger

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*
import java.util.function.Function
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.daemon.ZillaRuntime.empty

class JarTrimmer {
    val counter = IncludeCount("")

    class IncludeCount(val path: String) {
        var count: Int = 0
            private set

        val totalCount: Int
            get() {
                var total = count
                for (count in subPaths.values) total += count.totalCount
                return total
            }

        private val subPaths: MutableMap<String, IncludeCount>

        init {
            this.count = 0
            this.subPaths = HashMap()
        }

        fun incr() {
            count++
        }

        fun getCounter(path: String): IncludeCount {
            if (empty(path)) return this
            val slashPos = path.indexOf('/')
            val hasSlash = slashPos != -1
            val part = if (hasSlash) path.substring(0, slashPos) else path
            val subCount = (subPaths as java.util.Map<String, IncludeCount>).computeIfAbsent(part) { v -> IncludeCount(part) }
            return if (hasSlash) subCount.getCounter(path.substring(slashPos + 1)) else subCount
        }

        fun getSubPaths(): Map<String, IncludeCount> {
            return this.subPaths
        }
    }

    @Throws(Exception::class)
    fun trim(config: JarTrimmerConfig): IncludeCount? {

        val jar = JarFile(config.inJar!!)

        // walk all class resources in jar file, track location/count of required classes
        processJar(jar, { jarEntry ->
            val name = jarEntry.getName()
            if (name.endsWith(CLASS_SUFFIX)) {
                if (config.required(name)) counter.getCounter(toPath(name)).incr()
            }
            null
        })

        // Level 1: any packages that do not contain ANY required classes will not be included in the output jar
        val temp = FileUtil.temp(".jar")
        @Cleanup val jarOut = JarOutputStream(FileOutputStream(temp))
        val dirsCreated = HashSet<String>()
        processJar(jar, { jarEntry ->
            val name = jarEntry.getName()
            if (shouldInclude(config, name)) {
                try {
                    val dir = if (name.contains("/")) toPath(name) else null
                    if (dir != null && !dirsCreated.contains(dir)) {
                        val dirEntry = JarEntry("$dir/")
                        dirEntry.time = jarEntry.getTime()
                        jarOut.putNextEntry(dirEntry)
                        dirsCreated.add(dir)
                    }
                    val outEntry = JarEntry(jarEntry.getName())
                    ReflectionUtil.copy<JarEntry>(outEntry, jarEntry, arrayOf("method", "time", "size", "compressedSize", "crc"))
                    jarOut.putNextEntry(outEntry)
                    @Cleanup val `in` = jar.getInputStream(jarEntry)
                    IOUtils.copy(`in`, jarOut)

                } catch (e: Exception) {
                    return@processJar die<IncludeCount>("processJar: $e", e)
                }

            } else if (!name.endsWith("/")) {
                log.info("omitted: $name")
            }
            null
        })

        FileUtil.renameOrDie(temp, config.outJar)
        return counter
    }

    private fun shouldInclude(config: JarTrimmerConfig, name: String): Boolean {
        if (name.endsWith("/")) return false
        return if (config.required(name)) true else counter.getCounter(toPath(name)).totalCount > 0
    }

    private fun processJar(jar: JarFile, func: Function<JarEntry, IncludeCount>) {
        val enumeration = jar.entries()
        while (enumeration.hasMoreElements()) func.apply(enumeration.nextElement())
    }

    private fun toPath(jarEntryName: String): String {
        val lastSlash = jarEntryName.lastIndexOf('/')
        return if (lastSlash == -1 || lastSlash == jarEntryName.length - 1) jarEntryName else jarEntryName.substring(0, lastSlash)
    }

    companion object {

        val CLASS_SUFFIX = ".class"
        private val log = org.slf4j.LoggerFactory.getLogger(JarTrimmer::class.java)

        fun toClassName(jarEntryName: String): String {
            return jarEntryName.substring(0, jarEntryName.length - CLASS_SUFFIX.length).replace("/", ".")
        }
    }

}

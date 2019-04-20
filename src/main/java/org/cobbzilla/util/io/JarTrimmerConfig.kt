package org.cobbzilla.util.io

import com.fasterxml.jackson.annotation.JsonIgnore
import lombok.experimental.Accessors
import org.apache.commons.io.FileUtils
import org.cobbzilla.util.collection.ArrayUtil

import java.io.File
import java.io.IOException
import java.util.Arrays
import java.util.HashSet

import org.cobbzilla.util.string.StringUtil.UTF8cs

@Accessors(chain = true)
class JarTrimmerConfig {

    private var inJar: File? = null
    private var outJar: File? = null

    private var requiredClasses: Array<String>? = null
    @JsonIgnore
    val requiredClassSet: Set<String> = HashSet(Arrays.asList(*getRequiredClasses()!!))

    private var requiredPrefixes = arrayOf("META-INF", "WEB-INF")
    @JsonIgnore
    val requiredPrefixSet: Set<String> = HashSet(Arrays.asList(*getRequiredPrefixes()))

    private var includeRootFiles = true
    private var counterFile: File? = null
    fun getOutJar(): File? {
        return if (outJar != null) outJar else inJar
    }

    @Throws(IOException::class)
    fun setRequiredClassesFromFile(f: File): JarTrimmerConfig {
        requiredClasses = FileUtils.readLines(f, UTF8cs).toTypedArray()
        return this
    }

    fun requirePrefix(prefix: String): JarTrimmerConfig {
        requiredPrefixes = ArrayUtil.append(requiredPrefixes, prefix)
        return this
    }

    fun hasCounterFile(): Boolean {
        return counterFile != null
    }

    fun required(name: String): Boolean {
        return (requiredClassSet.contains(JarTrimmer.toClassName(name))
                || requiredByPrefix(name)
                || includeRootFiles && !name.contains("/"))
    }

    private fun requiredByPrefix(name: String): Boolean {
        for (prefix in requiredPrefixSet) if (name.startsWith(prefix)) return true
        return false
    }

    @Throws(IOException::class)
    fun requireClasses(file: File): JarTrimmerConfig {
        requiredClasses = ArrayUtil.append(requiredClasses, *FileUtils.readLines(file).toTypedArray())
        return this
    }

    fun getInJar(): File? {
        return this.inJar
    }

    fun getRequiredClasses(): Array<String>? {
        return this.requiredClasses
    }

    fun getRequiredPrefixes(): Array<String> {
        return this.requiredPrefixes
    }

    fun isIncludeRootFiles(): Boolean {
        return this.includeRootFiles
    }

    fun getCounterFile(): File? {
        return this.counterFile
    }

    fun setInJar(inJar: File): JarTrimmerConfig {
        this.inJar = inJar
        return this
    }

    fun setOutJar(outJar: File): JarTrimmerConfig {
        this.outJar = outJar
        return this
    }

    fun setRequiredClasses(requiredClasses: Array<String>): JarTrimmerConfig {
        this.requiredClasses = requiredClasses
        return this
    }

    fun setRequiredPrefixes(requiredPrefixes: Array<String>): JarTrimmerConfig {
        this.requiredPrefixes = requiredPrefixes
        return this
    }

    fun setIncludeRootFiles(includeRootFiles: Boolean): JarTrimmerConfig {
        this.includeRootFiles = includeRootFiles
        return this
    }

    fun setCounterFile(counterFile: File): JarTrimmerConfig {
        this.counterFile = counterFile
        return this
    }
}

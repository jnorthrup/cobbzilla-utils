package org.cobbzilla.util.io

import org.slf4j.Logger

import java.io.File
import java.util.HashSet
import java.util.concurrent.ConcurrentHashMap

import org.cobbzilla.util.io.FileUtil.abs
import org.cobbzilla.util.security.ShaUtil.sha256_file

class UniqueFileFsWalker(size: Int) : FilesystemVisitor {
    private val hash: MutableMap<String, Set<String>>

    init {
        hash = ConcurrentHashMap(size)
    }

    override fun visit(file: File) {
        val path = abs(file)
        log.debug(path)
        (hash as java.util.Map<String, Set<String>>).computeIfAbsent(sha256_file(file)) { k -> HashSet() }.add(path)
    }

    fun getHash(): Map<String, Set<String>> {
        return this.hash
    }

    companion object {

        private val log = org.slf4j.LoggerFactory.getLogger(UniqueFileFsWalker::class.java)
    }
}

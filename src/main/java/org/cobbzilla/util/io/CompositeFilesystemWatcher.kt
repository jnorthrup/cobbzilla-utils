package org.cobbzilla.util.io

import org.cobbzilla.util.string.StringUtil
import org.slf4j.Logger

import java.io.Closeable
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.util.ArrayList
import java.util.concurrent.ConcurrentHashMap

import org.cobbzilla.util.reflect.ReflectionUtil.getFirstTypeParam

abstract class CompositeFilesystemWatcher<T : FilesystemWatcher> : Closeable {
    private var watchers: MutableMap<Path, T>? = ConcurrentHashMap()

    val isEmpty: Boolean
        get() = watchers!!.isEmpty()

    @Throws(IOException::class)
    override fun close() {
        val copy = watchers
        watchers = null
        for (watcher in copy!!.values) {
            watcher.close()
        }
    }

    fun pathsWatching(): List<Path> {
        return ArrayList(watchers!!.keys)
    }

    fun dirsWatching(): List<File> {
        val paths = pathsWatching()
        val dirs = ArrayList<File>(paths.size)
        for (p in paths) dirs.add(p.toFile())
        return dirs
    }

    protected abstract fun newWatcher(path: Path): T

    fun add(path: String) {
        add(File(path))
    }

    fun add(path: File) {
        add(path.toPath())
    }

    fun add(path: Path) {
        val watcher = newWatcher(path)
        val old = watchers!!.remove(path)
        if (old != null) {
            log.warn("Replacing old watcher ($old) with new one: $watcher")
            old.stop()
        }
        watcher.start()
        watchers!![path] = watcher
    }

    fun addAll(paths: Array<File>?) {
        if (paths != null) for (p in paths) add(p)
    }

    fun addAll(paths: Array<Path>?) {
        if (paths != null) for (p in paths) add(p)
    }

    fun addAll(paths: Array<String>?) {
        if (paths != null) for (p in paths) add(File(p))
    }

    fun addAll(things: Collection<*>) {
        if (!things.isEmpty()) {
            val clazz = things.iterator().next().javaClass
            if (clazz == File::class.java) {
                addAll(things.toTypedArray() as Array<File>)
            } else if (clazz == Path::class.java) {
                addAll(things.toTypedArray() as Array<Path>)
            } else if (clazz == String::class.java) {
                addAll(things.toTypedArray() as Array<String>)
            }
        }
    }

    override fun toString(): String {
        return "CompositeFilesystemWatcher<" + getFirstTypeParam<Any>(javaClass).name + ">{" +
                "paths=" + StringUtil.toString(watchers!!.keys, " ") + "}"
    }

    companion object {

        private val log = org.slf4j.LoggerFactory.getLogger(CompositeFilesystemWatcher<*>::class.java)
    }
}

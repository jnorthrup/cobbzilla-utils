package org.cobbzilla.util.io

import org.cobbzilla.util.collection.StringSetSource

import java.io.File

class PathListFileResolver(paths: Collection<String>) : StringSetSource(), FileResolver {

    init {
        addValues(paths)
    }

    override fun resolve(path: String): File? {
        var path = path
        for (`val` in values) {
            if (!`val`.endsWith(File.separator)) `val` += File.separator
            if (path.startsWith(File.separator)) path = path.substring(File.separator.length)
            val f = File(`val` + path)
            if (f.exists() && f.canRead()) return f
        }
        return null
    }

}

package org.cobbzilla.util.io

import java.io.File
import java.io.FilenameFilter

class FilenameSuffixFilter @java.beans.ConstructorProperties("suffix")
constructor(var suffix: String?) : FilenameFilter {

    override fun accept(dir: File, name: String): Boolean {
        return name.endsWith(suffix!!)
    }
}

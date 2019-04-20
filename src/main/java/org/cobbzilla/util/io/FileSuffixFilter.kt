package org.cobbzilla.util.io

import java.io.File
import java.io.FileFilter

class FileSuffixFilter @java.beans.ConstructorProperties("suffix")
constructor(var suffix: String?) : FileFilter {

    override fun accept(pathname: File): Boolean {
        return pathname.name.endsWith(suffix!!)
    }
}

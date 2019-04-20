package org.cobbzilla.util.io

import java.io.File
import java.io.FileFilter

class RegularFileFilter : FileFilter {

    override fun accept(pathname: File): Boolean {
        return pathname.isFile
    }

    companion object {

        val instance = RegularFileFilter()
    }

}

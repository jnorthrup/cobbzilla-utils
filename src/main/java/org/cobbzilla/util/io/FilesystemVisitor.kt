package org.cobbzilla.util.io

import java.io.File

interface FilesystemVisitor {
    fun visit(file: File)
}

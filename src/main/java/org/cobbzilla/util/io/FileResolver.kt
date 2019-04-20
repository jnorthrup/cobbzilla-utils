package org.cobbzilla.util.io

import java.io.File

interface FileResolver {

    fun resolve(path: String): File

}

package org.cobbzilla.util.io

import java.io.File
import java.io.FileFilter
import java.util.regex.Pattern

import org.cobbzilla.util.daemon.ZillaRuntime.empty

class DirFilter : FileFilter {

    var regex: String? = null

    val pattern = initPattern()

    @java.beans.ConstructorProperties("regex")
    constructor(regex: String) {
        this.regex = regex
    }

    constructor() {}

    private fun initPattern(): Pattern {
        return Pattern.compile(regex!!)
    }

    override fun accept(pathname: File): Boolean {
        return pathname.isDirectory && (empty(regex) || pattern.matcher(pathname.name).matches())
    }

    companion object {

        val instance = DirFilter()
    }
}

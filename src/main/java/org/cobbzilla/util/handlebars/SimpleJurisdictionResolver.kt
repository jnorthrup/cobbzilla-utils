package org.cobbzilla.util.handlebars

import org.cobbzilla.util.string.StringUtil

import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.daemon.ZillaRuntime.empty

class SimpleJurisdictionResolver : JurisdictionResolver {

    override fun usState(value: String): String? {
        return if (empty(value) || value.length != 2) die<String>("usState: invalid: $value") else value.toUpperCase()
    }

    override fun usZip(value: String): String {
        return if (empty(value) || value.length != 5 || StringUtil.onlyDigits(value).length != value.length)
            die("usZip: invalid: $value")
        else
            value.toUpperCase()
    }

    companion object {

        val instance = SimpleJurisdictionResolver()
    }

}

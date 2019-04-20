package org.cobbzilla.util.handlebars

import org.cobbzilla.util.daemon.ZillaRuntime.empty

interface JurisdictionResolver {

    fun usState(value: String): String?

    fun usZip(value: String): String

    fun isValidUsStateAbbreviation(a: String): Boolean {
        return !empty(a) && usState(a) != null
    }

}

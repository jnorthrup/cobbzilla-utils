package org.cobbzilla.util.error

import org.cobbzilla.util.string.StringUtil

import org.cobbzilla.util.daemon.ZillaRuntime.die

interface GeneralErrorHandler {
    open fun <T> handleError(message: String): T {
        return die(message)
    }

    open fun <T> handleError(message: String, e: Exception): T {
        return die(message, e)
    }

    open fun <T> handleError(validationErrors: List<String>): T {
        return die("validation errors: " + StringUtil.toString(validationErrors))
    }
}

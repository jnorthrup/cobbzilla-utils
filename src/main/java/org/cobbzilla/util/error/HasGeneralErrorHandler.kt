package org.cobbzilla.util.error

import java.util.concurrent.atomic.AtomicReference

interface HasGeneralErrorHandler {

    val errorHandler: AtomicReference<GeneralErrorHandler>

    fun <T> error(message: String): T {
        return errorHandler.get().handleError(message)
    }

    fun <T> error(message: String, e: Exception): T {
        return errorHandler.get().handleError(message, e)
    }

}

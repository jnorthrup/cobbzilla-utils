package org.cobbzilla.util.error

import java.util.concurrent.atomic.AtomicReference

class GeneralErrorHandlerBase : GeneralErrorHandler {
    companion object {
        val instance = GeneralErrorHandlerBase()

        fun defaultErrorHandler(): AtomicReference<GeneralErrorHandler> {
            return AtomicReference(GeneralErrorHandlerBase.instance)
        }
    }
}

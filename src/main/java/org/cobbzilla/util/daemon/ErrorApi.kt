package org.cobbzilla.util.daemon

/**
 * A generic interface for error reporting services like Errbit and Airbrake
 */
interface ErrorApi {

    fun report(e: Exception)
    fun report(s: String)
    fun report(s: String, e: Exception)

}

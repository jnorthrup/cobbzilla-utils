package org.cobbzilla.util.jdbc

import java.sql.SQLException

class UncheckedSqlException @java.beans.ConstructorProperties("sqlException")
constructor(private val sqlException: SQLException) : RuntimeException() {

    override fun getMessage(): String {
        return sqlException.message
    }

    override fun getLocalizedMessage(): String {
        return sqlException.localizedMessage
    }

    @Synchronized
    override fun getCause(): Throwable {
        return sqlException.cause
    }

    @Synchronized
    override fun initCause(throwable: Throwable): Throwable {
        return sqlException.initCause(throwable)
    }

    override fun toString(): String {
        return sqlException.toString()
    }

}

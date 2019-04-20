package org.cobbzilla.util.jdbc;

import java.sql.SQLException;

public class UncheckedSqlException extends RuntimeException {

    private final SQLException sqlException;

    @java.beans.ConstructorProperties({"sqlException"})
    public UncheckedSqlException(SQLException sqlException) {
        this.sqlException = sqlException;
    }

    @Override public String getMessage() { return sqlException.getMessage(); }

    @Override public String getLocalizedMessage() { return sqlException.getLocalizedMessage(); }

    @Override public synchronized Throwable getCause() { return sqlException.getCause(); }

    @Override public synchronized Throwable initCause(Throwable throwable) { return sqlException.initCause(throwable); }

    @Override public String toString() { return sqlException.toString(); }

}

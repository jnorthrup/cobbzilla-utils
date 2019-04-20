package org.cobbzilla.util.jdbc;

import org.slf4j.Logger;

import java.sql.*;
import java.sql.Driver;
import java.util.Properties;

import static org.cobbzilla.util.reflect.ReflectionUtil.instantiate;

public class DebugPostgresqlDriver implements DebugDriver {

    private static final String DEBUG_PREFIX = "debug:";
    private static final String POSTGRESQL_PREFIX = "jdbc:postgresql:";
    private static final String DRIVER_CLASS_NAME = "org.postgresql.Driver";
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DebugPostgresqlDriver.class);

    static {
        try {
            java.sql.DriverManager.registerDriver(new DebugPostgresqlDriver().driver);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
 
    private Driver driver = instantiate(DRIVER_CLASS_NAME);
 

    public Connection connect(String s, Properties properties) throws SQLException {
        return driver.connect(s, properties);
    }

    private class MyDriver implements Driver {
        @Override public Connection connect(String url, Properties info) throws SQLException {
            if (url.startsWith(DEBUG_PREFIX)) {
                url = url.substring(DEBUG_PREFIX.length());
                if (url.startsWith(POSTGRESQL_PREFIX)) {
                    return new DebugConnection(driver.connect(url, info));
                }
            }
            throw new IllegalArgumentException("can't connect: "+url);
        }

        public boolean acceptsURL(String s) throws SQLException {
            return driver.acceptsURL(s);
        }

        public DriverPropertyInfo[] getPropertyInfo(String s, Properties properties) throws SQLException {
            return driver.getPropertyInfo(s, properties);
        }

        public int getMajorVersion() {
            return driver.getMajorVersion();
        }

        public int getMinorVersion() {
            return driver.getMinorVersion();
        }

        public boolean jdbcCompliant() {
            return driver.jdbcCompliant();
        }

        public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return driver.getParentLogger();
        }
    }
}

package org.cobbzilla.util.jdbc

import org.slf4j.Logger

import java.sql.*
import java.sql.Driver
import java.util.Properties

import org.cobbzilla.util.reflect.ReflectionUtil.instantiate

class DebugPostgresqlDriver : DebugDriver {

    private val driver = instantiate<Driver>(DRIVER_CLASS_NAME)


    @Throws(SQLException::class)
    override fun connect(s: String, properties: Properties): Connection {
        return driver.connect(s, properties)
    }

    private inner class MyDriver : Driver {
        @Throws(SQLException::class)
        override fun connect(url: String, info: Properties): Connection {
            var url = url
            if (url.startsWith(DEBUG_PREFIX)) {
                url = url.substring(DEBUG_PREFIX.length)
                if (url.startsWith(POSTGRESQL_PREFIX)) {
                    return DebugConnection(driver.connect(url, info))
                }
            }
            throw IllegalArgumentException("can't connect: $url")
        }

        @Throws(SQLException::class)
        override fun acceptsURL(s: String): Boolean {
            return driver.acceptsURL(s)
        }

        @Throws(SQLException::class)
        override fun getPropertyInfo(s: String, properties: Properties): Array<DriverPropertyInfo> {
            return driver.getPropertyInfo(s, properties)
        }

        override fun getMajorVersion(): Int {
            return driver.majorVersion
        }

        override fun getMinorVersion(): Int {
            return driver.minorVersion
        }

        override fun jdbcCompliant(): Boolean {
            return driver.jdbcCompliant()
        }

        @Throws(SQLFeatureNotSupportedException::class)
        override fun getParentLogger(): java.util.logging.Logger {
            return driver.parentLogger
        }
    }

    companion object {

        private val DEBUG_PREFIX = "debug:"
        private val POSTGRESQL_PREFIX = "jdbc:postgresql:"
        private val DRIVER_CLASS_NAME = "org.postgresql.Driver"
        private val log = org.slf4j.LoggerFactory.getLogger(DebugPostgresqlDriver::class.java)

        init {
            try {
                java.sql.DriverManager.registerDriver(DebugPostgresqlDriver().driver)
            } catch (e: SQLException) {
                e.printStackTrace()
            }

        }
    }
}

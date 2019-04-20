package org.cobbzilla.util.jdbc

import java.sql.Connection
import java.sql.SQLException
import java.util.Properties

interface DebugDriver {

    @Throws(SQLException::class)
    fun connect(url: String, info: Properties): Connection

}

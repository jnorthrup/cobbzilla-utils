package org.cobbzilla.util.jdbc

import java.util.regex.Matcher
import java.util.regex.Pattern

object DbUrlUtil {

    val JDBC_URL_REGEX = Pattern.compile("^jdbc:postgresql://[\\.\\w]+:\\d+/(.+)$")

    fun setDbName(url: String, dbName: String): String {
        val matcher = JDBC_URL_REGEX.matcher(url)
        return if (!matcher.find()) url else matcher.replaceFirst(dbName)
    }

}

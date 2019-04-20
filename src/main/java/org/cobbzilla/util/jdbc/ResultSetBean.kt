package org.cobbzilla.util.jdbc

import lombok.Cleanup
import org.slf4j.Logger

import java.sql.*
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.atomic.AtomicReference

open class ResultSetBean {

    val rows = ArrayList<Map<String, Any>>()

    val isEmpty: Boolean
        get() = rows.isEmpty()

    private val rsMetaData = AtomicReference<ResultSetMetaData>()

    private constructor() {}

    fun rowCount(): Int {
        return if (isEmpty) 0 else rows.size
    }

    fun first(): Map<String, Any> {
        return rows[0]
    }

    fun count(): Int? {
        return if (isEmpty) null else Integer.valueOf(rows[0].entries.iterator().next().value.toString())
    }

    fun countOrZero(): Int {
        return if (isEmpty) 0 else Integer.parseInt(rows[0].entries.iterator().next().value.toString())
    }

    @Throws(SQLException::class)
    constructor(rs: ResultSet) {
        rows.addAll(read(rs))
    }

    @Throws(SQLException::class)
    constructor(ps: PreparedStatement) {
        rows.addAll(read(ps))
    }

    @Throws(SQLException::class)
    constructor(conn: Connection, sql: String) {
        rows.addAll(read(conn, sql))
    }

    @Throws(SQLException::class)
    fun getRsMetaData(rs: ResultSet): ResultSetMetaData {
        if (rsMetaData.get() == null) {
            synchronized(rsMetaData) {
                if (rsMetaData.get() == null) {
                    rsMetaData.set(rs.metaData)
                }
            }
        }
        return rsMetaData.get()
    }

    fun getRsMetaData(): ResultSetMetaData {
        return rsMetaData.get()
    }

    @Throws(SQLException::class)
    private fun read(conn: Connection, sql: String): List<Map<String, Any>> {
        @Cleanup val ps = conn.prepareStatement(sql)
        return read(ps)
    }

    @Throws(SQLException::class)
    private fun read(ps: PreparedStatement): List<Map<String, Any>> {
        @Cleanup val rs = ps.executeQuery()
        return read(rs)
    }

    @Throws(SQLException::class)
    private fun read(rs: ResultSet): List<Map<String, Any>> {
        val rsMetaData = getRsMetaData(rs)
        val numColumns = rsMetaData.columnCount
        val results = ArrayList<Map<String, Any>>()
        while (rs.next()) {
            val row = row2map(rs, rsMetaData, numColumns)
            results.add(row)
        }
        return results
    }

    fun <T> getColumnValues(column: String): List<T> {
        val values = ArrayList<T>()
        for (row in rows) values.add(row[column] as T)
        return values
    }

    companion object {

        val EMPTY = ResultSetBean()
        private val log = org.slf4j.LoggerFactory.getLogger(ResultSetBean::class.java)

        @Throws(SQLException::class)
        fun row2map(rs: ResultSet): HashMap<String, Any> {
            val rsMetaData = rs.metaData
            val numColumns = rsMetaData.columnCount
            return row2map(rs, rsMetaData, numColumns)
        }

        @Throws(SQLException::class)
        fun row2map(rs: ResultSet, rsMetaData: ResultSetMetaData): HashMap<String, Any> {
            val numColumns = rsMetaData.columnCount
            return row2map(rs, rsMetaData, numColumns)
        }

        @Throws(SQLException::class)
        fun row2map(rs: ResultSet, rsMetaData: ResultSetMetaData, numColumns: Int): HashMap<String, Any> {
            val row = HashMap<String, Any>(numColumns)
            for (i in 1..numColumns) {
                row[rsMetaData.getColumnName(i)] = rs.getObject(i)
            }
            return row
        }

        @Throws(SQLException::class)
        fun getColumns(rsMetaData: ResultSetMetaData): List<String> {
            val columnCount = rsMetaData.columnCount
            val columns = ArrayList<String>(columnCount)
            for (i in 1..columnCount) {
                columns.add(rsMetaData.getColumnName(i))
            }
            return columns
        }
    }
}

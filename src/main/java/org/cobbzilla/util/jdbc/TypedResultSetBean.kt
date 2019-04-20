package org.cobbzilla.util.jdbc

import org.cobbzilla.util.reflect.ReflectionUtil
import org.slf4j.Logger

import java.lang.reflect.Field
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

import org.cobbzilla.util.reflect.ReflectionUtil.getDeclaredField
import org.cobbzilla.util.reflect.ReflectionUtil.instantiate
import org.cobbzilla.util.string.StringUtil.snakeCaseToCamelCase

class TypedResultSetBean<T> : ResultSetBean, Iterable<T> {

    private val rowType: Class<T>
    private var typedRows: List<T>? = null

    @Throws(SQLException::class)
    constructor(clazz: Class<T>, rs: ResultSet) : super(rs) {
        rowType = clazz
        init()
    }

    @Throws(SQLException::class)
    constructor(clazz: Class<T>, ps: PreparedStatement) : super(ps) {
        rowType = clazz
        init()
    }

    @Throws(SQLException::class)
    constructor(clazz: Class<T>, conn: Connection, sql: String) : super(conn, sql) {
        rowType = clazz
        init()
    }

    internal fun init() {
        val typedRows1 = ArrayList<T>()
        for (row in rows) {
            val thing = instantiate(rowType)
            for (name in row.keys) {
                val field = snakeCaseToCamelCase(name)
                try {
                    val value = row[name]
                    readField(thing, field, value)
                } catch (e: Exception) {
                    log.warn("getTypedRows: error setting $field: $e")
                }

            }
            typedRows1.add(thing)
        }
        typedRows = typedRows1
    }

    override fun iterator(): Iterator<T> {
        return ArrayList(typedRows!!).iterator()
    }

    protected fun readField(thing: T, field: String, value: Any?) {
        if (value != null) {
            try {
                ReflectionUtil.set(thing, field, value)
            } catch (e: Exception) {
                // try field setter
                try {
                    val f = getDeclaredField(thing.javaClass, field)
                    if (f != null) {
                        f.isAccessible = true
                        f.set(thing, value)
                    } else {
                        log.warn("readField: field " + thing.javaClass.getName() + "." + field + " not found via setter nor via field: " + e)
                    }
                } catch (e2: Exception) {
                    log.warn("readField: field " + thing.javaClass.getName() + "." + field + " not found via setter nor via field: " + e2)
                }

            }

        }
    }

    fun <K> map(field: String): Map<K, T> {
        val map = HashMap<K, T>()
        for (thing in this) {
            map[ReflectionUtil.get(thing, field) as K] = thing
        }
        return map
    }

    fun firstObject(): T? {
        val iter = iterator()
        return if (iter.hasNext()) iter.next() else null
    }

    companion object {

        private val log = org.slf4j.LoggerFactory.getLogger(TypedResultSetBean<*>::class.java)
    }

}

package org.cobbzilla.util.jdbc

import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.Logger

import java.sql.*
import java.util.Properties
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicInteger

class DebugConnection(private val connection: Connection) : Connection {

    private val id: Int

    init {
        this.id = counter.getAndIncrement()
        val msg = "DebugConnection " + id + " opened from " + ExceptionUtils.getStackTrace(Exception("opened"))
        log.info(msg)
    }


    @Throws(SQLException::class)
    override fun beginRequest() {
        connection.beginRequest()
    }

    @Throws(SQLException::class)
    override fun endRequest() {
        connection.endRequest()
    }

    @Throws(SQLException::class)
    override fun setShardingKeyIfValid(shardingKey: ShardingKey?, superShardingKey: ShardingKey?, timeout: Int): Boolean {
        return connection.setShardingKeyIfValid(shardingKey, superShardingKey, timeout)
    }

    @Throws(SQLException::class)
    override fun setShardingKeyIfValid(shardingKey: ShardingKey?, timeout: Int): Boolean {
        return connection.setShardingKeyIfValid(shardingKey, timeout)
    }

    @Throws(SQLException::class)
    override fun setShardingKey(shardingKey: ShardingKey?, superShardingKey: ShardingKey?) {
        connection.setShardingKey(shardingKey, superShardingKey)
    }

    @Throws(SQLException::class)
    override fun setShardingKey(shardingKey: ShardingKey?) {
        connection.setShardingKey(shardingKey)
    }

    @Throws(SQLException::class)
    override fun createStatement(): Statement {
        return connection.createStatement()
    }

    @Throws(SQLException::class)
    override fun prepareStatement(s: String): PreparedStatement {
        return connection.prepareStatement(s)
    }

    @Throws(SQLException::class)
    override fun prepareCall(s: String): CallableStatement {
        return connection.prepareCall(s)
    }

    @Throws(SQLException::class)
    override fun nativeSQL(s: String): String {
        return connection.nativeSQL(s)
    }

    @Throws(SQLException::class)
    override fun setAutoCommit(b: Boolean) {
        connection.autoCommit = b
    }

    @Throws(SQLException::class)
    override fun getAutoCommit(): Boolean {
        return connection.autoCommit
    }

    @Throws(SQLException::class)
    override fun commit() {
        connection.commit()
    }

    @Throws(SQLException::class)
    override fun rollback() {
        connection.rollback()
    }

    @Throws(SQLException::class)
    override fun close() {
        connection.close()
    }

    @Throws(SQLException::class)
    override fun isClosed(): Boolean {
        return connection.isClosed
    }

    @Throws(SQLException::class)
    override fun getMetaData(): DatabaseMetaData {
        return connection.metaData
    }

    @Throws(SQLException::class)
    override fun setReadOnly(b: Boolean) {
        connection.isReadOnly = b
    }

    @Throws(SQLException::class)
    override fun isReadOnly(): Boolean {
        return connection.isReadOnly
    }

    @Throws(SQLException::class)
    override fun setCatalog(s: String) {
        connection.catalog = s
    }

    @Throws(SQLException::class)
    override fun getCatalog(): String {
        return connection.catalog
    }

    @Throws(SQLException::class)
    override fun setTransactionIsolation(i: Int) {
        connection.transactionIsolation = i
    }

    @Throws(SQLException::class)
    override fun getTransactionIsolation(): Int {
        return connection.transactionIsolation
    }

    @Throws(SQLException::class)
    override fun getWarnings(): SQLWarning {
        return connection.warnings
    }

    @Throws(SQLException::class)
    override fun clearWarnings() {
        connection.clearWarnings()
    }

    @Throws(SQLException::class)
    override fun createStatement(i: Int, i1: Int): Statement {
        return connection.createStatement(i, i1)
    }

    @Throws(SQLException::class)
    override fun prepareStatement(s: String, i: Int, i1: Int): PreparedStatement {
        return connection.prepareStatement(s, i, i1)
    }

    @Throws(SQLException::class)
    override fun prepareCall(s: String, i: Int, i1: Int): CallableStatement {
        return connection.prepareCall(s, i, i1)
    }

    @Throws(SQLException::class)
    override fun getTypeMap(): Map<String, Class<*>> {
        return connection.typeMap
    }

    @Throws(SQLException::class)
    override fun setTypeMap(map: Map<String, Class<*>>) {
        connection.typeMap = map
    }

    @Throws(SQLException::class)
    override fun setHoldability(i: Int) {
        connection.holdability = i
    }

    @Throws(SQLException::class)
    override fun getHoldability(): Int {
        return connection.holdability
    }

    @Throws(SQLException::class)
    override fun setSavepoint(): Savepoint {
        return connection.setSavepoint()
    }

    @Throws(SQLException::class)
    override fun setSavepoint(s: String): Savepoint {
        return connection.setSavepoint(s)
    }

    @Throws(SQLException::class)
    override fun rollback(savepoint: Savepoint) {
        connection.rollback(savepoint)
    }

    @Throws(SQLException::class)
    override fun releaseSavepoint(savepoint: Savepoint) {
        connection.releaseSavepoint(savepoint)
    }

    @Throws(SQLException::class)
    override fun createStatement(i: Int, i1: Int, i2: Int): Statement {
        return connection.createStatement(i, i1, i2)
    }

    @Throws(SQLException::class)
    override fun prepareStatement(s: String, i: Int, i1: Int, i2: Int): PreparedStatement {
        return connection.prepareStatement(s, i, i1, i2)
    }

    @Throws(SQLException::class)
    override fun prepareCall(s: String, i: Int, i1: Int, i2: Int): CallableStatement {
        return connection.prepareCall(s, i, i1, i2)
    }

    @Throws(SQLException::class)
    override fun prepareStatement(s: String, i: Int): PreparedStatement {
        return connection.prepareStatement(s, i)
    }

    @Throws(SQLException::class)
    override fun prepareStatement(s: String, ints: IntArray): PreparedStatement {
        return connection.prepareStatement(s, ints)
    }

    @Throws(SQLException::class)
    override fun prepareStatement(s: String, strings: Array<String>): PreparedStatement {
        return connection.prepareStatement(s, strings)
    }

    @Throws(SQLException::class)
    override fun createClob(): Clob {
        return connection.createClob()
    }

    @Throws(SQLException::class)
    override fun createBlob(): Blob {
        return connection.createBlob()
    }

    @Throws(SQLException::class)
    override fun createNClob(): NClob {
        return connection.createNClob()
    }

    @Throws(SQLException::class)
    override fun createSQLXML(): SQLXML {
        return connection.createSQLXML()
    }

    @Throws(SQLException::class)
    override fun isValid(i: Int): Boolean {
        return connection.isValid(i)
    }

    @Throws(SQLClientInfoException::class)
    override fun setClientInfo(s: String, s1: String) {
        connection.setClientInfo(s, s1)
    }

    @Throws(SQLClientInfoException::class)
    override fun setClientInfo(properties: Properties) {
        connection.clientInfo = properties
    }

    @Throws(SQLException::class)
    override fun getClientInfo(s: String): String {
        return connection.getClientInfo(s)
    }

    @Throws(SQLException::class)
    override fun getClientInfo(): Properties {
        return connection.clientInfo
    }

    @Throws(SQLException::class)
    override fun createArrayOf(s: String, objects: Array<Any>): Array {
        return connection.createArrayOf(s, objects)
    }

    @Throws(SQLException::class)
    override fun createStruct(s: String, objects: Array<Any>): Struct {
        return connection.createStruct(s, objects)
    }

    @Throws(SQLException::class)
    override fun setSchema(s: String) {
        connection.schema = s
    }

    @Throws(SQLException::class)
    override fun getSchema(): String {
        return connection.schema
    }

    @Throws(SQLException::class)
    override fun abort(executor: Executor) {
        connection.abort(executor)
    }

    @Throws(SQLException::class)
    override fun setNetworkTimeout(executor: Executor, i: Int) {
        connection.setNetworkTimeout(executor, i)
    }

    @Throws(SQLException::class)
    override fun getNetworkTimeout(): Int {
        return connection.networkTimeout
    }

    @Throws(SQLException::class)
    override fun <T> unwrap(aClass: Class<T>): T {
        return connection.unwrap(aClass)
    }

    @Throws(SQLException::class)
    override fun isWrapperFor(aClass: Class<*>): Boolean {
        return connection.isWrapperFor(aClass)
    }

    companion object {

        private val counter = AtomicInteger(0)
        private val log = org.slf4j.LoggerFactory.getLogger(DebugConnection::class.java)
    }

}

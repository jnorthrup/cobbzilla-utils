package org.cobbzilla.util.io

import java.io.IOException
import java.io.InputStream

class ByteLimitedInputStream(private val delegate: InputStream, val limit: Long) : InputStream() {

    var count: Long = 0
        private set

    val percentDone: Double
        get() = count.toDouble() / limit.toDouble()

    private interface BLISDelegateExcludes {
        @Throws(IOException::class)
        fun read(b: ByteArray): Int

        @Throws(IOException::class)
        fun read(b: ByteArray, off: Int, len: Int): Int

        @Throws(IOException::class)
        fun read(): Int
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray): Int {
        if (count >= limit) return -1
        val read = delegate.read(b)
        if (read != -1) count += read.toLong()
        return read
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (count >= limit) return -1
        val read = delegate.read(b, off, len)
        if (read != -1) count += read.toLong()
        return read
    }

    @Throws(IOException::class)
    override fun read(): Int {
        if (count >= limit) return -1
        val read = delegate.read()
        if (read != -1) count++
        return read
    }

}

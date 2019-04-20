package org.cobbzilla.util.daemon

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.commons.lang3.RandomUtils
import org.apache.commons.lang3.SystemUtils
import org.cobbzilla.util.collection.ToStringTransformer
import org.cobbzilla.util.error.GeneralErrorHandler
import org.cobbzilla.util.io.StreamUtil
import org.cobbzilla.util.reflect.ReflectionUtil
import org.cobbzilla.util.string.StringUtil
import org.slf4j.Logger

import java.io.*
import java.lang.management.ManagementFactory
import java.lang.reflect.Array
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

import java.lang.Long.toHexString
import java.util.stream.LongStream.range
import org.apache.commons.collections.CollectionUtils.collect
import org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace
import org.cobbzilla.util.io.FileUtil.abs
import org.cobbzilla.util.io.FileUtil.list
import org.cobbzilla.util.system.Sleep.sleep

/**
 * the Zilla doesn't mess around.
 */
object ZillaRuntime {

    val CLASSPATH_PREFIX = "classpath:"
    private val log = org.slf4j.LoggerFactory.getLogger(ZillaRuntime::class.java)

    var errorApi: ErrorApi? = null

    @Volatile
    var systemTimeOffset: Long = 0

    val OMIT_DEBUG_OPTIONS = arrayOf("-Xdebug", "-agentlib", "-Xrunjdwp")

    private val selfDestructInitiated = AtomicBoolean(false)

    fun terminate(thread: Thread?, timeout: Long) {
        if (thread == null || !thread.isAlive) return
        thread.interrupt()
        val start = realNow()
        while (thread.isAlive && realNow() - start < timeout) {
            sleep(100, "terminate: waiting for thread to die: $thread")
        }
        if (thread.isAlive) {
            log.warn("terminate: thread did not die voluntarily, killing it: $thread")
            thread.stop()
        }
    }

    fun background(r: Runnable): Thread {
        val t = Thread(r)
        t.start()
        return t
    }

    fun daemon(r: Runnable): Thread {
        val t = Thread(r)
        t.isDaemon = true
        t.start()
        return t
    }

    fun <T> die(message: String): T {
        return _throw(IllegalStateException(message, null))
    }

    fun <T> die(message: String, e: Exception): T {
        return _throw(IllegalStateException(message, e))
    }

    fun <T> die(e: Exception): T {
        return _throw(IllegalStateException("(no message)", e))
    }

    fun <T> notSupported(): T {
        return notSupported("not supported")
    }

    fun <T> notSupported(message: String): T {
        return _throw(UnsupportedOperationException(message))
    }

    private fun <T> _throw(e: RuntimeException): T {
        val message = e.message
        val cause = e.cause
        if (errorApi != null) {
            if (cause != null && cause is Exception)
                errorApi!!.report(message, cause as Exception?)
            else
                errorApi!!.report(e)
        }
        if (cause != null) log.error("Inner exception: $message", cause)
        throw e
    }

    fun empty(s: String?): Boolean {
        return s == null || s.length == 0
    }

    /**
     * Determines if the parameter is "empty", by criteria described in @return
     * Tries to avoid throwing exceptions, handling just about any case in a true/false fashion.
     *
     * @param o anything
     * @return true if and only o is:
     * * null
     * * a collection, map, iterable or array that contains no objects
     * * a file that does not exist or whose size is zero
     * * a directory that does not exist or that contains no files
     * * any object whose .toString method returns a zero-length string
     */
    fun empty(o: Any?): Boolean {
        if (o == null) return true
        if (o is String) return o.toString().length == 0
        if (o is Collection<*>) return o.isEmpty()
        if (o is Map<*, *>) return o.isEmpty()
        if (o is JsonNode) {
            if (o is ObjectNode) return o.size() == 0
            if (o is ArrayNode) return o.size() == 0
            val json = o.textValue()
            return json == null || json.length == 0
        }
        if (o is Iterable<*>) return !o.iterator().hasNext()
        if (o is File) {
            val f = o as File?
            return !f!!.exists() || f.length() == 0L || f.isDirectory && list(f).size == 0
        }
        return if (o.javaClass.isArray) {
            if (o.javaClass.componentType.isPrimitive) {
                when (o.javaClass.componentType.name) {
                    "boolean" -> (o as BooleanArray).size == 0
                    "byte" -> (o as ByteArray).size == 0
                    "short" -> (o as ShortArray).size == 0
                    "char" -> (o as CharArray).size == 0
                    "int" -> (o as IntArray).size == 0
                    "long" -> (o as LongArray).size == 0
                    "float" -> (o as FloatArray).size == 0
                    "double" -> (o as DoubleArray).size == 0
                    else -> o.toString().length == 0
                }
            } else {
                (o as Array<Any>).size == 0
            }
        } else o.toString().length == 0
    }

    fun <T> sorted(thing: T): T? {
        if (empty(thing)) return thing
        if (thing.javaClass.isArray()) {
            val copy = Array.newInstance(thing.javaClass.getComponentType(),
                    (thing as Array<Any>).size) as Array<Any>
            System.arraycopy(thing, 0, copy, 0, copy.size)
            Arrays.sort(copy)
            return copy
        }
        if (thing is Collection<*>) {
            val list = ArrayList(thing as Collection<*>)
            Collections.sort<Comparable>(list)
            val copy = ReflectionUtil.instantiate(thing.javaClass) as Collection<*>
            copy.addAll(list)
            return copy as T
        }
        return die<T>("sorted: cannot sort a " + thing.javaClass.getSimpleName() + ", can only sort arrays and Collections")
    }

    fun <T> sortedList(thing: T?): List<*>? {
        if (thing == null) return null
        if (thing is Collection<*>) return ArrayList((thing as Collection<*>?)!!)
        return if (thing is Array<Any>) Arrays.asList<Any>(*(thing as Array<Any>?)!!) else die<List<*>>("sortedList: cannot sort a " + thing.javaClass.getSimpleName() + ", can only sort arrays and Collections")
    }

    @JvmOverloads
    fun safeBoolean(`val`: String, ifNull: Boolean? = null): Boolean? {
        return if (empty(`val`)) ifNull else java.lang.Boolean.valueOf(`val`)
    }

    @JvmOverloads
    fun safeInt(`val`: String, ifNull: Int? = null): Int? {
        return if (empty(`val`)) ifNull else Integer.valueOf(`val`)
    }

    @JvmOverloads
    fun safeLong(`val`: String, ifNull: Long? = null): Long? {
        return if (empty(`val`)) ifNull else java.lang.Long.valueOf(`val`)
    }

    fun bigint(`val`: Long): BigInteger {
        return BigInteger(`val`.toString())
    }

    fun bigint(`val`: Int): BigInteger {
        return BigInteger(`val`.toString())
    }

    fun bigint(`val`: Byte): BigInteger {
        return BigInteger(`val`.toString())
    }

    fun big(`val`: String): BigDecimal {
        return BigDecimal(`val`)
    }

    fun big(`val`: Double): BigDecimal {
        return BigDecimal(`val`.toString())
    }

    fun big(`val`: Float): BigDecimal {
        return BigDecimal(`val`.toString())
    }

    fun big(`val`: Long): BigDecimal {
        return BigDecimal(`val`.toString())
    }

    fun big(`val`: Int): BigDecimal {
        return BigDecimal(`val`.toString())
    }

    fun big(`val`: Byte): BigDecimal {
        return BigDecimal(`val`.toString())
    }

    @JvmOverloads
    fun percent(value: Int, pct: Double, rounding: RoundingMode = RoundingMode.HALF_UP): Int {
        return big(value).multiply(big(pct)).setScale(0, rounding).toInt()
    }

    fun percent(value: BigDecimal, pct: BigDecimal): Int {
        return percent(value.toInt(), pct.multiply(big(0.01)).toDouble(), RoundingMode.HALF_UP)
    }

    fun uuid(): String {
        return UUID.randomUUID().toString()
    }

    fun now(): Long {
        return System.currentTimeMillis() + systemTimeOffset
    }

    fun hexnow(): String {
        return toHexString(now())
    }

    fun hexnow(now: Long): String {
        return toHexString(now)
    }

    fun realNow(): Long {
        return System.currentTimeMillis()
    }

    fun <T> pickRandom(things: Array<T>): T {
        return things[RandomUtils.nextInt(0, things.size)]
    }

    fun <T> pickRandom(things: List<T>): T {
        return things[RandomUtils.nextInt(0, things.size)]
    }

    fun stdin(): BufferedReader {
        return BufferedReader(InputStreamReader(System.`in`))
    }

    fun stdout(): BufferedWriter {
        return BufferedWriter(OutputStreamWriter(System.out))
    }

    fun readStdin(): String {
        return StreamUtil.toStringOrDie(System.`in`)
    }

    fun envInt(name: String, defaultValue: Int, maxValue: Int?): Int {
        return envInt(name, defaultValue, null, maxValue)
    }

    @JvmOverloads
    fun envInt(name: String, defaultValue: Int, minValue: Int? = null, maxValue: Int? = null, env: Map<String, String> = System.getenv()): Int {
        val s = env[name]
        if (!empty(s)) {
            try {
                val `val` = Integer.parseInt(s)
                if (`val` <= 0) {
                    log.warn("envInt: invalid value($name): $`val`, returning $defaultValue")
                    return defaultValue
                } else if (maxValue != null && `val` > maxValue) {
                    log.warn("envInt: value too large ($name): $`val`, returning $maxValue")
                    return maxValue
                } else if (minValue != null && `val` < minValue) {
                    log.warn("envInt: value too small ($name): $`val`, returning $minValue")
                    return minValue
                }
                return `val`
            } catch (e: Exception) {
                log.warn("envInt: invalid value($name): $s, returning $defaultValue")
                return defaultValue
            }

        }
        return defaultValue
    }

    fun processorCount(): Int {
        return Runtime.getRuntime().availableProcessors()
    }

    fun hashOf(vararg things: Any): String {
        val b = StringBuilder()
        for (thing in things) {
            b.append(thing ?: "null").append(":::")
        }
        return b.toString()
    }

    fun stringRange(start: Number, end: Number): Collection<String> {
        return collect(range(start.toLong(), end.toLong()).boxed().iterator(), ToStringTransformer.instance)
    }

    fun zcat(): String {
        return if (SystemUtils.IS_OS_MAC) "gzcat" else "zcat"
    }

    fun zcat(f: File): String {
        return (if (SystemUtils.IS_OS_MAC) "gzcat" else "zcat") + " " + abs(f)
    }

    fun isDebugOption(arg: String): Boolean {
        for (opt in OMIT_DEBUG_OPTIONS) if (arg.startsWith(opt)) return true
        return false
    }

    @JvmOverloads
    fun javaOptions(excludeDebugOptions: Boolean = true): String {
        val opts = ArrayList<String>()
        for (arg in ManagementFactory.getRuntimeMXBean().inputArguments) {
            if (excludeDebugOptions && isDebugOption(arg)) continue
            opts.add(arg)
        }
        return StringUtil.toString(opts, " ")
    }

    fun <T> dcl(target: AtomicReference<T>, init: Callable<T>): T? {
        return dcl(target, init, null)
    }

    fun <T> dcl(target: AtomicReference<T>, init: Callable<T>, error: GeneralErrorHandler?): T? {
        if (target.get() == null) {
            synchronized(target) {
                if (target.get() == null) {
                    try {
                        target.set(init.call())
                    } catch (e: Exception) {
                        if (error != null) {
                            error.handleError<Any>("dcl: error initializing: $e", e)
                        } else {
                            log.warn("dcl: $e")
                            return null
                        }
                    }

                }
            }
        }
        return target.get()
    }

    fun stacktrace(): String {
        return getStackTrace(Exception())
    }

    @JvmOverloads
    fun setSelfDestruct(t: Long, status: Int = 0) {
        synchronized(selfDestructInitiated) {
            if (!selfDestructInitiated.get()) {
                daemon({
                    sleep(t)
                    System.exit(status)
                })
                selfDestructInitiated.set(true)
            } else {
                log.warn("setSelfDestruct: already set!")
            }
        }
    }
}

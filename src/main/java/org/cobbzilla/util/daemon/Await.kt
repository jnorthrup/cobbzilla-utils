package org.cobbzilla.util.daemon

import org.cobbzilla.util.time.ClockProvider
import org.slf4j.Logger

import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.daemon.ZillaRuntime.now
import org.cobbzilla.util.system.Sleep.sleep

object Await {

    val DEFAULT_AWAIT_GET_SLEEP: Long = 10
    val DEFAULT_AWAIT_RETRY_SLEEP: Long = 100
    private val log = org.slf4j.LoggerFactory.getLogger(Await::class.java)

    @Throws(TimeoutException::class)
    fun <E> awaitFirst(futures: MutableCollection<Future<E>>, timeout: Long): E? {
        return awaitFirst(futures, timeout, DEFAULT_AWAIT_RETRY_SLEEP)
    }

    @Throws(TimeoutException::class)
    fun <E> awaitFirst(futures: MutableCollection<Future<E>>, timeout: Long, retrySleep: Long): E? {
        return awaitFirst(futures, timeout, retrySleep, DEFAULT_AWAIT_GET_SLEEP)
    }

    @Throws(TimeoutException::class)
    fun <E> awaitFirst(futures: MutableCollection<Future<E>>, timeout: Long, retrySleep: Long, getSleep: Long): E? {
        val start = now()
        while (!futures.isEmpty() && now() - start < timeout) {
            val iter = futures.iterator()
            while (iter.hasNext()) {
                val future = iter.next()
                try {
                    val value = future.get(getSleep, TimeUnit.MILLISECONDS)
                    if (value != null) return value
                    iter.remove()
                    if (futures.isEmpty()) break

                } catch (e: InterruptedException) {
                    die<Any>("await: interrupted: $e")
                } catch (e: ExecutionException) {
                    die<Any>("await: execution error: $e")
                } catch (e: TimeoutException) {
                    // noop
                }

                sleep(retrySleep)
            }
        }
        if (now() - start > timeout) throw TimeoutException("await: timed out")
        return null // all futures had a null result
    }

    @Throws(TimeoutException::class)
    fun awaitAndCollect(futures: MutableList<Future<List<*>>>, maxQueryResults: Int, timeout: Long, results: MutableList<*>): List<*> {
        return awaitAndCollect(futures, maxQueryResults, timeout, DEFAULT_AWAIT_RETRY_SLEEP, DEFAULT_AWAIT_GET_SLEEP, results)
    }

    @Throws(TimeoutException::class)
    @JvmOverloads
    fun awaitAndCollect(futures: MutableCollection<Future<List<*>>>, maxResults: Int, timeout: Long, retrySleep: Long = DEFAULT_AWAIT_RETRY_SLEEP, getSleep: Long = DEFAULT_AWAIT_GET_SLEEP, results: MutableList<*> = ArrayList()): List<*> {
        val start = now()
        var size = futures.size
        while (!futures.isEmpty() && now() - start < timeout) {
            val iter = futures.iterator()
            while (iter.hasNext()) {
                val future = iter.next()
                try {
                    results.addAll(future.get(getSleep, TimeUnit.MILLISECONDS) as List<*>)
                    iter.remove()
                    if (--size <= 0 || results.size >= maxResults) return results
                    break

                } catch (e: InterruptedException) {
                    die<Any>("await: interrupted: $e")
                } catch (e: ExecutionException) {
                    die<Any>("await: execution error: $e")
                } catch (e: TimeoutException) {
                    // noop
                }

                sleep(retrySleep)
            }
        }
        if (now() - start > timeout) throw TimeoutException("await: timed out")
        return results
    }

    @Throws(TimeoutException::class)
    fun awaitAndCollectSet(futures: MutableList<Future<List<*>>>, maxQueryResults: Int, timeout: Long, results: MutableSet<*>): Set<*> {
        return awaitAndCollectSet(futures, maxQueryResults, timeout, DEFAULT_AWAIT_RETRY_SLEEP, DEFAULT_AWAIT_GET_SLEEP, results)
    }

    @Throws(TimeoutException::class)
    @JvmOverloads
    fun awaitAndCollectSet(futures: MutableCollection<Future<List<*>>>, maxResults: Int, timeout: Long, retrySleep: Long = DEFAULT_AWAIT_RETRY_SLEEP, getSleep: Long = DEFAULT_AWAIT_GET_SLEEP, results: MutableSet<*> = HashSet()): Set<*> {
        val start = now()
        var size = futures.size
        while (!futures.isEmpty() && now() - start < timeout) {
            val iter = futures.iterator()
            while (iter.hasNext()) {
                val future = iter.next()
                try {
                    results.addAll(future.get(getSleep, TimeUnit.MILLISECONDS) as Collection<*>)
                    iter.remove()
                    if (--size <= 0 || results.size >= maxResults) return results
                    break

                } catch (e: InterruptedException) {
                    die<Any>("await: interrupted: $e")
                } catch (e: ExecutionException) {
                    die<Any>("await: execution error: $e")
                } catch (e: TimeoutException) {
                    // noop
                }

                sleep(retrySleep)
            }
        }
        if (now() - start > timeout) throw TimeoutException("await: timed out")
        return results
    }

    fun <T> awaitAll(futures: Collection<Future<*>>, timeout: Long): AwaitResult<T> {
        return awaitAll(futures, timeout, ClockProvider.SYSTEM)
    }

    fun <T> awaitAll(futures: Collection<Future<*>>, timeout: Long, clock: ClockProvider): AwaitResult<T> {
        val start = clock.now()
        val result = AwaitResult<T>()
        val awaiting = ArrayList(futures)

        while (clock.now() - start < timeout) {
            val iter = awaiting.iterator()
            while (iter.hasNext()) {
                val f = iter.next()
                if (f.isDone) {
                    iter.remove()
                    try {
                        val r = f.get() as T
                        if (r != null) log.info("awaitAll: $r")
                        result.success(f, r)

                    } catch (e: Exception) {
                        log.warn("awaitAll: $e", e)
                        result.fail(f, e)
                    }

                }
            }
            if (awaiting.isEmpty()) break
            sleep(200)
        }

        result.timeout(awaiting)
        return result
    }
}

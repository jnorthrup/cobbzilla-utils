package org.cobbzilla.util.daemon

import com.fasterxml.jackson.annotation.JsonIgnore

import java.util.*
import java.util.concurrent.Future

class AwaitResult<T> {

    private val successes = HashMap<Future<*>, T>()

    private val failures = HashMap<Future<*>, Exception>()

    private val timeouts = ArrayList<Future<*>>()

    val notNullSuccesses: List<T>
        @JsonIgnore get() {
            val ok = ArrayList<T>()
            for (t in getSuccesses().values) if (t != null) ok.add(t)
            return ok
        }

    fun success(f: Future<*>, thing: T) {
        successes[f] = thing
    }

    fun numSuccesses(): Int {
        return successes.size
    }

    fun fail(f: Future<*>, e: Exception) {
        failures[f] = e
    }

    fun numFails(): Int {
        return failures.size
    }

    fun timeout(timedOut: Collection<Future<*>>) {
        timeouts.addAll(timedOut)
    }

    fun timedOut(): Boolean {
        return !timeouts.isEmpty()
    }

    fun numTimeouts(): Int {
        return timeouts.size
    }

    fun allSucceeded(): Boolean {
        return failures.isEmpty() && timeouts.isEmpty()
    }

    override fun toString(): String {
        return ("successes=" + successes.size
                + ", failures=" + failures.size
                + ", timeouts=" + timeouts.size)
    }

    fun getSuccesses(): Map<Future<*>, T> {
        return this.successes
    }

    fun getFailures(): Map<Future<*>, Exception> {
        return this.failures
    }

    fun getTimeouts(): List<Future<*>> {
        return this.timeouts
    }
}

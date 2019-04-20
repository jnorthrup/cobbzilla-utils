package org.cobbzilla.util.collection.multi

import java.util.ArrayList
import java.util.LinkedHashMap

class MultiResult {

    var successes: MutableList<String> = ArrayList()
    var failures: MutableMap<String, String> = LinkedHashMap()

    val header: String
        get() = "TEST RESULTS"

    fun successCount(): Int {
        return successes.size
    }

    fun failCount(): Int {
        return failures.size
    }

    fun success(name: String) {
        successes.add(name)
    }

    fun fail(name: String, reason: String) {
        failures[name] = reason
    }

    fun hasFailures(): Boolean {
        return !failures.isEmpty()
    }

    override fun toString(): String {
        val b = StringBuilder()
        b.append("\n\n").append(header).append("\n--------------------\n")
                .append(successCount()).append("\tsucceeded\n")
                .append(failCount()).append("\tfailed")
        if (!failures.isEmpty()) {
            b.append(":\n")
            for (fail in failures.keys) {
                b.append(fail).append("\n")
            }
            b.append("--------------------\n")
            b.append("\nfailure details:\n")
            for ((key, value) in failures) {
                b.append(key).append(":\t").append(value).append("\n")
                b.append("--------\n")
            }
        } else {
            b.append("\n")
        }
        b.append("--------------------\n")
        return b.toString()
    }

}

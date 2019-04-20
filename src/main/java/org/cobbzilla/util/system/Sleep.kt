package org.cobbzilla.util.system

import org.slf4j.Logger

object Sleep {

    private val log = org.slf4j.LoggerFactory.getLogger(Sleep::class.java)

    /**
     * sleep and throw an exception if interrupted
     * @param millis how long to sleep
     * @param reason something to add to the log statement if we are interrupted
     */
    @JvmOverloads
    fun sleep(millis: Long = 100, reason: String = "no reason for sleep given") {
        try {
            Thread.sleep(millis)
        } catch (e: InterruptedException) {
            throw IllegalStateException("sleep interrupted ($reason)")
        }

    }

    /**
     * A nap is something that you might get interrupted doing.
     * @param millis how long to nap
     * @param reason something to add to the log statement if we are interrupted
     * @return true if you napped without being interrupted, false if you were interrupted
     */
    @JvmOverloads
    fun nap(millis: Long, reason: String = "no reason for nap given"): Boolean {
        try {
            Thread.sleep(millis)
            return true
        } catch (e: InterruptedException) {
            log.info("nap ($reason): interrupted")
            return false
        }

    }
}
/**
 * sleep for 100ms and throw an exception if interrupted
 */
/**
 * sleep and throw an exception if interrupted
 * @param millis how long to sleep
 */
/**
 * A nap is something that you might get interrupted doing.
 * @param millis how long to nap
 * @return true if you napped without being interrupted, false if you were interrupted
 */

package org.cobbzilla.util.collection

import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import lombok.experimental.Accessors
import lombok.extern.slf4j.Slf4j
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

import org.cobbzilla.util.daemon.ZillaRuntime.now

@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Slf4j
class FailedOperationCounter<T> : ConcurrentHashMap<T, Map<Long, Long>> {

    @Getter
    @Setter
    val expiration = TimeUnit.MINUTES.toMillis(5)
    @Getter
    @Setter
    val maxFailures = 1

    fun fail(value: T) {
        var failures: MutableMap<Long, Long>? = get(value)
        if (failures == null) {
            failures = ConcurrentHashMap()
            put(value, failures)
        }
        val ftime = now()
        failures[ftime] = ftime
    }

    fun tooManyFailures(value: T): Boolean {
        val failures = get(value) ?: return false
        var count = 0
        val iter = failures.keys.iterator()
        while (iter.hasNext()) {
            val ftime = iter.next()
            if (now() - ftime > expiration) {
                iter.remove()
            } else {
                if (++count >= maxFailures) return true
            }
        }
        return false
    }
}

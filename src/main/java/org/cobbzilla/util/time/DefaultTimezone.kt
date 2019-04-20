package org.cobbzilla.util.time

import org.joda.time.DateTimeZone
import org.slf4j.Logger

import org.cobbzilla.util.io.StreamUtil.stream2string

object DefaultTimezone {

    val DEFAULT_TIMEZONE = "US/Eastern"

    val zone = initTimeZone()
    private val log = org.slf4j.LoggerFactory.getLogger(DefaultTimezone::class.java)

    private fun initTimeZone(): DateTimeZone {
        // first line that does not start with '#' within 'timezone.txt' resource file will be used
        try {
            val lines = stream2string("timezone.txt").split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (line in lines) if (!line.trim { it <= ' ' }.startsWith("#")) return DateTimeZone.forID(line.trim { it <= ' ' })
            log.warn("initTimeZone: error, timezone.txt resource did not contain a valid timezone line, using default: $DEFAULT_TIMEZONE")
            return DateTimeZone.forID(DEFAULT_TIMEZONE)

        } catch (e: Exception) {
            log.warn("initTimeZone: error, returning default (" + DEFAULT_TIMEZONE + "): " + e.javaClass.simpleName + ": " + e.message)
            return DateTimeZone.forID(DEFAULT_TIMEZONE)
        }

    }
}

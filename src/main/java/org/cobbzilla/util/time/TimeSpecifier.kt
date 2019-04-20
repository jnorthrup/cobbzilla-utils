package org.cobbzilla.util.time

import org.joda.time.DateTime
import org.joda.time.DurationFieldType

import org.cobbzilla.util.daemon.ZillaRuntime.now

interface TimeSpecifier {

    operator fun get(t: Long): Long

    companion object {

        fun nowSpecifier(): TimeSpecifier {
            return { t -> now() }
        }

        fun todaySpecifier(): TimeSpecifier {
            return { t -> DateTime(t, DefaultTimezone.zone).withTimeAtStartOfDay().millis }
        }

        fun pastDaySpecifier(count: Int): TimeSpecifier {
            return { t -> DateTime(t, DefaultTimezone.zone).withTimeAtStartOfDay().withFieldAdded(DurationFieldType.days(), -1 * count).millis }
        }

        fun yesterdaySpecifier(): TimeSpecifier {
            return pastDaySpecifier(1)
        }

        fun futureDaySpecifier(count: Int): TimeSpecifier {
            return { t -> DateTime(t, DefaultTimezone.zone).withTimeAtStartOfDay().withFieldAdded(DurationFieldType.days(), count).millis }
        }

        fun tomorrowSpecifier(): TimeSpecifier {
            return futureDaySpecifier(1)
        }
    }

}

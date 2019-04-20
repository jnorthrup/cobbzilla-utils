package org.cobbzilla.util.time

import org.cobbzilla.util.string.StringUtil
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.DurationFieldType
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder

import java.util.concurrent.TimeUnit

import org.cobbzilla.util.daemon.ZillaRuntime.*

object TimeUtil {

    val DAY = TimeUnit.DAYS.toMillis(1)
    val HOUR = TimeUnit.HOURS.toMillis(1)
    val MINUTE = TimeUnit.MINUTES.toMillis(1)
    val SECOND = TimeUnit.SECONDS.toMillis(1)

    val DATE_FORMAT_MMDDYYYY = DateTimeFormat.forPattern("MM/dd/yyyy")
    val DATE_FORMAT_MMMM_D_YYYY = DateTimeFormat.forPattern("MMMM d, yyyy")
    val DATE_FORMAT_YYYY_MM_DD = DateTimeFormat.forPattern("yyyy-MM-dd")
    val DATE_FORMAT_YYYYMMDD = DateTimeFormat.forPattern("yyyyMMdd")
    val DATE_FORMAT_MMM_DD_YYYY = DateTimeFormat.forPattern("MMM dd, yyyy")
    val DATE_FORMAT_YYYY_MM_DD_HH_mm_ss = DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss")
    val DATE_FORMAT_YYYYMMDDHHMMSS = DateTimeFormat.forPattern("yyyyMMddHHmmss")
    val DATE_FORMAT_HYPHEN_MMDDYYYY = DateTimeFormat.forPattern("MM-dd-yyyy")

    val DATE_TIME_FORMATS = arrayOf(DATE_FORMAT_YYYY_MM_DD, DATE_FORMAT_YYYY_MM_DD, DATE_FORMAT_YYYYMMDD, DATE_FORMAT_YYYY_MM_DD_HH_mm_ss, DATE_FORMAT_YYYYMMDDHHMMSS, DATE_FORMAT_HYPHEN_MMDDYYYY, DATE_FORMAT_MMDDYYYY)

    // For now only m (months) and d (days) are supported
    // Both have to be present at the same time in that same order, but the value for each can be 0 to exclude that one - e.g. 0m15d.
    val PERIOD_FORMATTER = PeriodFormatterBuilder()
            .appendMonths().appendSuffix("m").appendDays().appendSuffix("d").toFormatter()

    fun parse(time: String, formatter: DateTimeFormatter): Long? {
        return if (empty(time)) null else formatter.parseDateTime(time).millis
    }

    fun parse(time: String, formatter: DateTimeFormatter, timeZone: DateTimeZone): Long? {
        return if (empty(time)) null else formatter.withZone(timeZone).parseDateTime(time).millis
    }

    fun parse(`val`: String): Any? {
        for (f in DATE_TIME_FORMATS) {
            try {
                return TimeUtil.parse(`val`, f)
            } catch (ignored: Exception) {
                // noop
            }

        }
        return null
    }

    fun parse(`val`: String, timeZone: DateTimeZone): Long? {
        for (f in DATE_TIME_FORMATS) {
            try {
                return TimeUtil.parse(`val`, f, timeZone)
            } catch (ignored: Exception) {
                // noop
            }

        }
        return null
    }

    fun format(time: Long?, formatter: DateTimeFormatter): String? {
        return if (time == null) null else DateTime(time).toString(formatter)
    }

    fun formatDurationFrom(start: Long): String {
        val duration = now() - start
        return formatDuration(duration)
    }

    fun formatDuration(duration: Long): String {
        var duration = duration
        val negative = duration < 0
        if (negative) duration *= -1L
        val prefix = if (negative) "-" else ""

        var days: Long = 0
        var hours: Long = 0
        var mins: Long = 0
        var secs: Long = 0
        var millis: Long = 0

        if (duration > DAY) {
            days = duration / DAY
            duration -= days * DAY
        }
        if (duration > HOUR) {
            hours = duration / HOUR
            duration -= hours * HOUR
        }
        if (duration > MINUTE) {
            mins = duration / MINUTE
            duration -= mins * MINUTE
        }
        if (duration > SECOND) {
            secs = duration / SECOND
        }
        millis = duration - secs * SECOND

        return if (days > 0) prefix + String.format("%1$01dd %2$02d:%3$02d:%4$02d.%5$04d", days, hours, mins, secs, millis) else prefix + String.format("%1$02d:%2$02d:%3$02d.%4$04d", hours, mins, secs, millis)
    }

    fun parseDuration(duration: String): Long {
        if (empty(duration)) return 0
        val `val` = java.lang.Long.parseLong(if (duration.length > 1) StringUtil.chopSuffix(duration) else duration)
        when (duration[duration.length - 1]) {
            's' -> return TimeUnit.SECONDS.toMillis(`val`)
            'm' -> return TimeUnit.MINUTES.toMillis(`val`)
            'h' -> return TimeUnit.HOURS.toMillis(`val`)
            'd' -> return TimeUnit.DAYS.toMillis(`val`)
            else -> return `val`
        }
    }

    fun addYear(time: Long): Long {
        return DateTime(time).withFieldAdded(DurationFieldType.years(), 1).millis
    }

    fun add365days(time: Long): Long {
        return DateTime(time).withFieldAdded(DurationFieldType.days(), 365).millis
    }

    @JvmOverloads
    fun timestamp(clock: ClockProvider = ClockProvider.ZILLA): String {
        val now = clock.now()
        return DATE_FORMAT_YYYY_MM_DD.print(now) + "-" + hexnow(now)
    }

    fun startOfWeekMillis(): Long {
        return startOfWeek().millis
    }

    @JvmOverloads
    fun startOfWeek(zone: DateTimeZone = DefaultTimezone.zone): DateTime {
        val startOfToday = DateTime(zone).withTimeAtStartOfDay()
        return startOfToday.withFieldAdded(DurationFieldType.days(), -1 * startOfToday.dayOfWeek)
    }

    fun startOfMonthMillis(): Long {
        return startOfMonth().millis
    }

    @JvmOverloads
    fun startOfMonth(zone: DateTimeZone = DefaultTimezone.zone): DateTime {
        val startOfToday = DateTime(zone).withTimeAtStartOfDay()
        return startOfToday.withFieldAdded(DurationFieldType.days(), -1 * startOfToday.dayOfMonth)
    }

    fun startOfQuarter(t: DateTime): DateTime {
        val month = t.monthOfYear
        if (month <= 3) return t.withMonthOfYear(1)
        if (month <= 6) return t.withMonthOfYear(4)
        return if (month <= 9) t.withMonthOfYear(7) else t.withMonthOfYear(10)
    }

    fun startOfQuarterMillis(): Long {
        return startOfQuarter().millis
    }

    @JvmOverloads
    fun startOfQuarter(zone: DateTimeZone = DefaultTimezone.zone): DateTime {
        return startOfQuarter(DateTime(zone).withTimeAtStartOfDay())
    }

    fun startOfYearMillis(): Long {
        return startOfYear().millis
    }

    @JvmOverloads
    fun startOfYear(zone: DateTimeZone = DefaultTimezone.zone): DateTime {
        return DateTime(zone).withTimeAtStartOfDay().withMonthOfYear(1).withDayOfMonth(1)
    }

    fun yesterdayMillis(): Long {
        return yesterday().millis
    }

    @JvmOverloads
    fun yesterday(zone: DateTimeZone = DefaultTimezone.zone): DateTime {
        return DateTime(zone).withTimeAtStartOfDay().withFieldAdded(DurationFieldType.days(), -1)
    }

    fun lastWeekMillis(): Long {
        return lastWeek().millis
    }

    @JvmOverloads
    fun lastWeek(zone: DateTimeZone = DefaultTimezone.zone): DateTime {
        return DateTime(zone).withTimeAtStartOfDay().withFieldAdded(DurationFieldType.days(), -7).withDayOfWeek(1)
    }

    fun lastMonthMillis(): Long {
        return lastMonth().millis
    }

    @JvmOverloads
    fun lastMonth(zone: DateTimeZone = DefaultTimezone.zone): DateTime {
        return DateTime(zone).withTimeAtStartOfDay().withFieldAdded(DurationFieldType.months(), -1).withDayOfMonth(1)
    }

    fun lastQuarterMillis(): Long {
        return lastQuarter().millis
    }

    @JvmOverloads
    fun lastQuarter(zone: DateTimeZone = DefaultTimezone.zone): DateTime {
        return startOfQuarter(DateTime(zone).withTimeAtStartOfDay().withFieldAdded(DurationFieldType.months(), -3))
    }

    fun lastYearMillis(): Long {
        return lastYear().millis
    }

    @JvmOverloads
    fun lastYear(zone: DateTimeZone = DefaultTimezone.zone): DateTime {
        return DateTime(zone).withTimeAtStartOfDay().withFieldAdded(DurationFieldType.years(), -1).withDayOfYear(1)
    }

}

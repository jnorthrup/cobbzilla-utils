package org.cobbzilla.util.time

import com.fasterxml.jackson.annotation.JsonCreator
import org.slf4j.Logger

import java.util.Arrays

import org.cobbzilla.util.daemon.ZillaRuntime.now
import org.cobbzilla.util.time.TimeRelativeType.*
import org.cobbzilla.util.time.TimeSpecifier.*
import org.cobbzilla.util.time.TimeUtil.*

enum class TimePeriodType @java.beans.ConstructorProperties("type", "startSpecifier", "endSpecifier")
private constructor(val type: TimeRelativeType, private val startSpecifier: TimeSpecifier, private val endSpecifier: TimeSpecifier) {

    today(present, todaySpecifier(), tomorrowSpecifier()),
    tomorrow(future, tomorrowSpecifier(), futureDaySpecifier(2)),
    week_to_date(past, { t -> startOfWeekMillis() }, nowSpecifier()),
    month_to_date(past, { t -> startOfMonthMillis() }, nowSpecifier()),
    quarter_to_date(past, { t -> startOfQuarterMillis() }, nowSpecifier()),
    year_to_date(past, { t -> startOfYearMillis() }, nowSpecifier()),
    yesterday(past, yesterdaySpecifier(), todaySpecifier()),
    previous_week(past, { t -> lastWeekMillis() }, { t -> startOfWeekMillis() }),
    previous_month(past, { t -> lastWeekMillis() }, { t -> startOfMonthMillis() }),
    previous_quarter(past, { t -> lastQuarterMillis() }, { t -> startOfQuarterMillis() }),
    previous_year(past, { t -> lastYearMillis() }, { t -> startOfYearMillis() });

    fun start(): Long {
        return startSpecifier.get(now())
    }

    fun end(): Long {
        return endSpecifier.get(now())
    }

    companion object {

        private val log = org.slf4j.LoggerFactory.getLogger(TimePeriodType::class.java)

        @JsonCreator
        fun fromString(`val`: String): TimePeriodType {
            try {
                return valueOf(`val`.toLowerCase())
            } catch (e: IllegalArgumentException) {
                log.warn("fromString(" + `val` + "): invalid value, use one of: " + Arrays.toString(values()))
                throw e
            }

        }

        val pastTypes = arrayOf(today, week_to_date, month_to_date, quarter_to_date, year_to_date, yesterday, previous_week, previous_month, previous_quarter, previous_year)

        val futureTypes = arrayOf(today, tomorrow)
    }
}

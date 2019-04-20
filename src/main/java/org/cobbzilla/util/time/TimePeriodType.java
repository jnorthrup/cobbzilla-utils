package org.cobbzilla.util.time;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.slf4j.Logger;

import java.util.Arrays;

import static org.cobbzilla.util.daemon.ZillaRuntime.now;
import static org.cobbzilla.util.time.TimeRelativeType.*;
import static org.cobbzilla.util.time.TimeSpecifier.*;
import static org.cobbzilla.util.time.TimeUtil.*;

public enum TimePeriodType {

    today            (present, todaySpecifier(),            tomorrowSpecifier()),
    tomorrow         (future,  tomorrowSpecifier(),         futureDaySpecifier(2)),
    week_to_date     (past,    t -> startOfWeekMillis(),    nowSpecifier()),
    month_to_date    (past,    t -> startOfMonthMillis(),   nowSpecifier()),
    quarter_to_date  (past,    t -> startOfQuarterMillis(), nowSpecifier()),
    year_to_date     (past,    t -> startOfYearMillis(),    nowSpecifier()),
    yesterday        (past,    yesterdaySpecifier(),        todaySpecifier()),
    previous_week    (past,    t -> lastWeekMillis(),       t -> startOfWeekMillis()),
    previous_month   (past,    t -> lastWeekMillis(),       t -> startOfMonthMillis()),
    previous_quarter (past,    t -> lastQuarterMillis(),    t -> startOfQuarterMillis()),
    previous_year    (past,    t -> lastYearMillis(),       t -> startOfYearMillis());

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TimePeriodType.class);
    private TimeRelativeType type;
    private TimeSpecifier startSpecifier;
    private TimeSpecifier endSpecifier;

    @java.beans.ConstructorProperties({"type", "startSpecifier", "endSpecifier"})
    private TimePeriodType(TimeRelativeType type, TimeSpecifier startSpecifier, TimeSpecifier endSpecifier) {
        this.type = type;
        this.startSpecifier = startSpecifier;
        this.endSpecifier = endSpecifier;
    }

    @JsonCreator public static TimePeriodType fromString (String val) {
        try {
            return valueOf(val.toLowerCase());
        } catch (IllegalArgumentException e) {
            log.warn("fromString("+val+"): invalid value, use one of: "+Arrays.toString(values()));
            throw e;
        }
    }

    private static TimePeriodType[] pastTypes = {
            today, week_to_date, month_to_date, quarter_to_date, year_to_date,
            yesterday, previous_week, previous_month, previous_quarter, previous_year
    };

    private static TimePeriodType[] futureTypes = {
            today, tomorrow
    };

    public static TimePeriodType[] getPastTypes() {
        return TimePeriodType.pastTypes;
    }

    public static TimePeriodType[] getFutureTypes() {
        return TimePeriodType.futureTypes;
    }

    public long start() { return startSpecifier.get(now()); }
    public long end  () { return endSpecifier.get(now()); }

    public TimeRelativeType getType() {
        return this.type;
    }
}

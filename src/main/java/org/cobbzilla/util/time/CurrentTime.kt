package org.cobbzilla.util.time

import org.cobbzilla.util.daemon.ZillaRuntime
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class CurrentTime {

    var zone: String? = null
    var now: CurrentTimeValues? = null
    var realNow: CurrentTimeValues? = null

    constructor(tz: DateTimeZone) {
        zone = tz.id
        now = CurrentTimeValues(tz, ZillaRuntime.now())
        realNow = if (ZillaRuntime.systemTimeOffset == 0L) null else CurrentTimeValues(tz, ZillaRuntime.realNow())
    }

    constructor() {}

    class CurrentTimeValues {
        var now: Long = 0
        var yyyyMMdd: String? = null
        var yyyyMMddHHmmss: String? = null

        constructor(tz: DateTimeZone, now: Long) {
            this.now = now
            val time = DateTime(now, tz)
            yyyyMMdd = TimeUtil.DATE_FORMAT_YYYY_MM_DD.print(time)
            yyyyMMddHHmmss = TimeUtil.DATE_FORMAT_YYYY_MM_DD_HH_mm_ss.print(time)
        }

        constructor() {}
    }

}

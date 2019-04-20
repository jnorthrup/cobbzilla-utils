package org.cobbzilla.util.time;

import org.cobbzilla.util.daemon.ZillaRuntime;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class CurrentTime {

    private String zone;
    private CurrentTimeValues now;
    private CurrentTimeValues realNow;

    public CurrentTime(DateTimeZone tz) {
        zone = tz.getID();
        now = new CurrentTimeValues(tz, ZillaRuntime.now());
        realNow = ZillaRuntime.getSystemTimeOffset() == 0 ? null : new CurrentTimeValues(tz, ZillaRuntime.realNow());
    }

    public CurrentTime() {
    }

    public String getZone() {
        return this.zone;
    }

    public CurrentTimeValues getNow() {
        return this.now;
    }

    public CurrentTimeValues getRealNow() {
        return this.realNow;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public void setNow(CurrentTimeValues now) {
        this.now = now;
    }

    public void setRealNow(CurrentTimeValues realNow) {
        this.realNow = realNow;
    }

    public static class CurrentTimeValues {
        private long now;
        private String yyyyMMdd;
        private String yyyyMMddHHmmss;

        public CurrentTimeValues(DateTimeZone tz, long now) {
            this.now = now;
            final DateTime time = new DateTime(now, tz);
            yyyyMMdd = TimeUtil.DATE_FORMAT_YYYY_MM_DD.print(time);
            yyyyMMddHHmmss = TimeUtil.DATE_FORMAT_YYYY_MM_DD_HH_mm_ss.print(time);
        }

        public CurrentTimeValues() {
        }

        public long getNow() {
            return this.now;
        }

        public String getYyyyMMdd() {
            return this.yyyyMMdd;
        }

        public String getYyyyMMddHHmmss() {
            return this.yyyyMMddHHmmss;
        }

        public void setNow(long now) {
            this.now = now;
        }

        public void setYyyyMMdd(String yyyyMMdd) {
            this.yyyyMMdd = yyyyMMdd;
        }

        public void setYyyyMMddHHmmss(String yyyyMMddHHmmss) {
            this.yyyyMMddHHmmss = yyyyMMddHHmmss;
        }
    }

}

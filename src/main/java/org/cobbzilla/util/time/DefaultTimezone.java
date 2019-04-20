package org.cobbzilla.util.time;

import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

import static org.cobbzilla.util.io.StreamUtil.stream2string;

public class DefaultTimezone {

    public static final String DEFAULT_TIMEZONE = "US/Eastern";

    private static final DateTimeZone zone = initTimeZone();
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DefaultTimezone.class);

    private static DateTimeZone initTimeZone() {
        // first line that does not start with '#' within 'timezone.txt' resource file will be used
        try {
            final String[] lines = stream2string("timezone.txt").split("\n");
            for (String line : lines) if (!line.trim().startsWith("#")) return DateTimeZone.forID(line.trim());
            log.warn("initTimeZone: error, timezone.txt resource did not contain a valid timezone line, using default: "+DEFAULT_TIMEZONE);
            return DateTimeZone.forID(DEFAULT_TIMEZONE);

        } catch (Exception e) {
            log.warn("initTimeZone: error, returning default ("+DEFAULT_TIMEZONE+"): "+e.getClass().getSimpleName()+": "+e.getMessage());
            return DateTimeZone.forID(DEFAULT_TIMEZONE);
        }
    }

    public static DateTimeZone getZone() {
        return DefaultTimezone.zone;
    }
}

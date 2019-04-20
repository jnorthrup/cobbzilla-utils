package org.cobbzilla.util.time

import org.cobbzilla.util.io.StreamUtil
import org.cobbzilla.util.string.StringUtil
import org.slf4j.Logger

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.time.ImprovedTimezone.Companion.TIMEZONES_BY_ID
import org.cobbzilla.util.time.ImprovedTimezone.Companion.TIMEZONES_BY_JNAME

class ImprovedTimezone private constructor(val id: Int,
                                           val gmtOffset: String,
                                           val timezone: TimeZone,
                                           val displayName: String,
                                           linuxName: String?) {
    val linuxName: String
    val displayNameWithOffset: String

    init {
        this.linuxName = linuxName ?: timezone.displayName
        this.displayNameWithOffset = "($gmtOffset) $displayName"
    }

    fun getLocalTime(systemTime: Long): Long {
        // convert time to GMT
        val gmtTime = systemTime - SYSTEM_TIMEZONE!!.rawOffset

        // now that we're in GMT, convert to local
        return gmtTime + timezone.rawOffset
    }

    override fun toString(): String {
        return ("[ImprovedTimezone id=" + id + " offset=" + gmtOffset
                + " name=" + displayName + " zone=" + timezone.displayName + "]")
    }

    companion object {

        private val log = org.slf4j.LoggerFactory.getLogger(ImprovedTimezone::class.java)

        private var TIMEZONES: MutableList<ImprovedTimezone>? = null
        private val TIMEZONES_BY_ID = HashMap<Int, ImprovedTimezone>()
        private val TIMEZONES_BY_GMT = HashMap<String, ImprovedTimezone>()
        private val TIMEZONES_BY_JNAME = HashMap<String, ImprovedTimezone>()
        private val TZ_FILE = StringUtil.packagePath(ImprovedTimezone::class.java) + "/timezones.txt"

        private var SYSTEM_TIMEZONE: TimeZone? = null

        init {
            try {
                init()
            } catch (e: IOException) {
                val msg = "Error initializing ImprovedTimezone from timezones.txt: $e"
                log.error(msg, e)
                die<Any>(msg, e)
            }

            val sysTimezone = TimeZone.getDefault()
            var tz = TIMEZONES_BY_JNAME[sysTimezone.displayName]
            if (tz == null) {
                for (displayName in TIMEZONES_BY_JNAME.keys) {
                    val tz1 = TIMEZONES_BY_JNAME[displayName]
                    var dn = displayName.replace("GMT-0", "GMT-")
                    dn = dn.replace("GMT+0", "GMT+")
                    if (tz1.gmtOffset == dn) {
                        tz = tz1
                        break
                    }
                }
            }
            if (tz == null) {
                throw ExceptionInInitializerError("System Timezone could not be located in timezones.txt")
            }

            SYSTEM_TIMEZONE = tz.timezone
            log.info("System Time Zone set to " + SYSTEM_TIMEZONE!!.displayName)
        }

        val timeZones: List<ImprovedTimezone>?
            get() = TIMEZONES

        fun getTimeZoneById(id: Int): ImprovedTimezone {
            return TIMEZONES_BY_ID[id] ?: throw IllegalArgumentException("Invalid timezone id: $id")
        }

        fun getTimeZoneByJavaDisplayName(name: String): ImprovedTimezone {
            return TIMEZONES_BY_JNAME[name] ?: throw IllegalArgumentException("Invalid timezone name: $name")
        }

        fun getTimeZoneByGmtOffset(value: String): ImprovedTimezone {
            return TIMEZONES_BY_GMT[value]
        }

        /**
         * Initialize timezones from a file on classpath.
         * The first line of the file is a header that is ignored.
         */
        @Throws(IOException::class)
        private fun init() {

            TIMEZONES = ArrayList()
            StreamUtil.loadResourceAsStream(TZ_FILE).use { `in` ->
                if (`in` == null) {
                    throw IOException("Error loading timezone file from classpath: $TZ_FILE")
                }
                BufferedReader(InputStreamReader(`in`)).use { r ->
                    var line: String? = r.readLine()
                    while (line != null) {
                        line = r.readLine()
                        if (line == null) break
                        val improvedTimezone = initZone(line)
                        TIMEZONES!!.add(improvedTimezone)
                        TIMEZONES_BY_ID[improvedTimezone.id] = improvedTimezone
                        TIMEZONES_BY_JNAME[improvedTimezone.timezone.displayName] = improvedTimezone
                        TIMEZONES_BY_GMT[improvedTimezone.gmtOffset] = improvedTimezone
                    }
                }
            }
        }

        private fun initZone(line: String): ImprovedTimezone {
            try {
                val st = StringTokenizer(line, "|")
                val id = Integer.parseInt(st.nextToken())
                val gmtOffset = st.nextToken()
                val timezoneName = st.nextToken()
                val displayName = st.nextToken()
                val linuxName = if (st.hasMoreTokens()) st.nextToken() else timezoneName
                val tz = TimeZone.getTimeZone(timezoneName)
                if (gmtOffset != "GMT" && isGMT(tz)) {
                    val msg = "Error looking up timezone: $timezoneName: got GMT, expected $gmtOffset"
                    log.error(msg)
                    die<Any>(msg)
                }
                return ImprovedTimezone(id, gmtOffset, tz, displayName, linuxName)

            } catch (e: Exception) {
                return die("Error processing line: $line: $e", e)
            }

        }

        private fun isGMT(tz: TimeZone): Boolean {
            return tz.rawOffset == 0
        }
    }
}

package org.cobbzilla.util.string

import com.google.common.base.CaseFormat
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.collections.Transformer
import org.apache.commons.io.input.ReaderInputStream
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.LocaleUtils
import org.apache.commons.lang3.StringUtils
import org.cobbzilla.util.javascript.JsEngine
import org.cobbzilla.util.javascript.JsEngineConfig
import org.cobbzilla.util.security.MD5Util
import org.cobbzilla.util.time.ImprovedTimezone
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat

import java.io.StringReader
import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URISyntaxException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Function
import java.util.regex.Matcher
import java.util.regex.Pattern

import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.cobbzilla.util.collection.ArrayUtil.arrayToString
import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.daemon.ZillaRuntime.empty
import org.cobbzilla.util.io.StreamUtil.loadResourceAsStringOrDie

object StringUtil {

    val UTF8 = "UTF-8"
    val UTF8cs = Charset.forName(UTF8)

    val EMPTY = ""
    val EMPTY_ARRAY = arrayOf<String>()
    val DEFAULT_LOCALE = "en_US"
    val BYTES_PATTERN = "(\\d+)(\\p{javaSpaceChar}+)?([MgGgTtPpEe][Bb])"
    val CRLF = "\r\n"

    val XFORM_TO_STRING = { o -> o.toString() }

    val VOWELS = arrayOf("e", "a", "o", "i", "u")

    val HOUR = TimeUnit.HOURS.toMillis(1)
    val MINUTE = TimeUnit.MINUTES.toMillis(1)
    val SECOND = TimeUnit.SECONDS.toMillis(1)

    val ESCAPE_SQL = { o -> "'" + o.toString().replace("\'", "\'\'") + "'" }

    val NUMBER_FORMAT = NumberFormat.getInstance()

    private val DIFF_JS = (loadResourceAsStringOrDie(getPackagePath(StringUtil::class.java) + "/diff_match_patch.js") + "\n"
            + loadResourceAsStringOrDie(getPackagePath(StringUtil::class.java) + "/calc_diff.js") + "\n")
    var DIFF_JS_ENGINE = JsEngine(JsEngineConfig(5, 20, null))

    val PCT = "%"
    val ESC_PCT = "[%]"
    fun isVowel(symbol: String): Boolean {
        return ArrayUtils.indexOf(VOWELS, symbol) != -1
    }

    fun toStringCollection(c: Collection<*>): List<String> {
        return ArrayList(CollectionUtils.collect(c, XFORM_TO_STRING))
    }

    fun prefix(s: String?, count: Int): String? {
        return if (s == null) null else if (s.length > count) s.substring(0, count) else s
    }

    fun packagePath(clazz: Class<*>): String {
        return clazz.getPackage().name.replace(".", "/")
    }

    fun packagePath(clazz: String): String {
        return clazz.replace(".", "/")
    }

    fun split(s: String, delim: String): List<String> {
        val st = StringTokenizer(s, delim)
        val results = ArrayList<String>()
        while (st.hasMoreTokens()) {
            results.add(st.nextToken())
        }
        return results
    }

    fun split2array(s: String, delim: String): Array<String> {
        val vals = split(s, delim)
        return vals.toTypedArray()
    }

    fun splitLongs(s: String, delim: String): List<Long> {
        val st = StringTokenizer(s, delim)
        val results = ArrayList<Long>()
        while (st.hasMoreTokens()) {
            val token = st.nextToken()
            results.add(if (empty(token) || token.equals("null", ignoreCase = true)) null else java.lang.Long.parseLong(token))
        }
        return results
    }

    fun splitAndTrim(s: String, delim: String): List<String> {
        val results = ArrayList<String>()
        if (empty(s)) return results
        val st = StringTokenizer(s, delim)
        while (st.hasMoreTokens()) {
            results.add(st.nextToken().trim { it <= ' ' })
        }
        return results
    }

    fun replaceLast(s: String, find: String, replace: String): String? {
        if (empty(s)) return s
        val lastIndex = s.lastIndexOf(find)
        return if (lastIndex < 0) s else s.substring(0, lastIndex) + s.substring(lastIndex).replaceFirst(find.toRegex(), replace)
    }

    fun lastPathElement(url: String): String {
        return url.substring(url.lastIndexOf("/") + 1)
    }

    fun safeShellArg(s: String): String {
        return s.replace("[^-_\\w]+".toRegex(), "")
    }

    fun safeFunctionName(s: String): String {
        return s.replace("\\W".toRegex(), "")
    }

    fun safeSnakeName(s: String): String {
        return s.replace("\\W".toRegex(), "_")
    }

    fun onlyDigits(s: String): String {
        return s.replace("\\D+".toRegex(), "")
    }

    fun removeWhitespace(s: String): String {
        return s.replace("\\p{javaSpaceChar}".toRegex(), "")
    }

    fun safeParseInt(s: String): Int? {
        if (empty(s)) return null
        try {
            return Integer.parseInt(s)
        } catch (e: NumberFormatException) {
            return null
        }

    }

    fun safeParseDouble(s: String): Double? {
        if (empty(s)) return null
        try {
            return java.lang.Double.parseDouble(s)
        } catch (e: NumberFormatException) {
            return null
        }

    }

    fun shortDateTime(localeString: String, timezone: Int?, time: Long): String {
        return formatDateTime("SS", localeString, timezone, time)
    }

    fun mediumDateTime(localeString: String, timezone: Int?, time: Long): String {
        return formatDateTime("MM", localeString, timezone, time)
    }

    fun fullDateTime(localeString: String, timezone: Int?, time: Long): String {
        return formatDateTime("FF", localeString, timezone, time)
    }

    fun formatDateTime(style: String, localeString: String, timezone: Int?, time: Long): String {
        val locale = LocaleUtils.toLocale(localeString)
        val tz = ImprovedTimezone.getTimeZoneById(timezone!!)
        return DateTimeFormat.forPattern(DateTimeFormat.patternForStyle(style, locale))
                .withZone(DateTimeZone.forTimeZone(tz.timezone)).print(time)
    }

    fun chopSuffix(`val`: String): String {
        return `val`.substring(0, `val`.length - 1)
    }

    fun chopToFirst(`val`: String, find: String): String {
        return if (!`val`.contains(find)) `val` else `val`.substring(`val`.indexOf(find) + find.length)
    }

    fun trimQuotes(s: String?): String? {
        var s = s
        if (s == null) return s
        while (s!!.startsWith("\"") || s.startsWith("\'")) s = s.substring(1)
        while (s!!.endsWith("\"") || s.endsWith("\'")) s = s.substring(0, s.length - 1)
        return s
    }

    fun endsWithAny(s: String?, suffixes: Array<String>): Boolean {
        if (s == null) return false
        for (suffix in suffixes) if (s.endsWith(suffix)) return true
        return false
    }

    fun getPackagePath(clazz: Class<*>): String {
        return clazz.getPackage().name.replace('.', '/')
    }

    fun repeat(s: String, n: Int): String {
        return String(CharArray(n * s.length)).replace("\u0000", s)
    }

    fun urlEncode(s: String): String {
        try {
            return URLEncoder.encode(s, UTF8)
        } catch (e: UnsupportedEncodingException) {
            return die("urlEncode: $e", e)
        }

    }

    fun simpleUrlEncode(s: String): String {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    }

    fun urlDecode(s: String): String {
        try {
            return URLDecoder.decode(s, UTF8)
        } catch (e: UnsupportedEncodingException) {
            return die("urlDecode: $e", e)
        }

    }

    fun uriOrDie(s: String): URI {
        try {
            return URI(s)
        } catch (e: URISyntaxException) {
            return die("bad uri: $e", e)
        }

    }

    fun urlParameterize(params: Map<String, String>): String {
        val sb = StringBuilder()
        for ((key, value) in params) {
            if (sb.length > 0) sb.append('&')
            sb.append(urlEncode(key))
                    .append('=')
                    .append(urlEncode(value))
        }
        return sb.toString()
    }

    @JvmOverloads
    fun toString(c: Collection<*>, sep: String = ",", transformer: Function<Any, String>? = null): String {
        val builder = StringBuilder()
        for (o in c) {
            if (builder.length > 0) builder.append(sep)
            builder.append(transformer?.apply(o) ?: o)
        }
        return builder.toString()
    }

    fun sqlIn(c: Collection<*>): String {
        return toString(c, ",", ESCAPE_SQL)
    }

    fun toString(map: Map<*, *>?): String {
        if (map == null) return "null"
        val b = StringBuilder("{")
        for (key in map.keys) {
            val value = map[key]
            b.append(key).append("=")
            if (value == null) {
                b.append("null")
            } else {
                if (value.javaClass.isArray) {
                    b.append(arrayToString(value as Array<Any>?, ", "))
                } else if (value is Map<*, *>) {
                    b.append(toString(value as Map<*, *>?))
                } else if (value is Collection<*>) {
                    b.append(toString(value as Collection<*>?, ", "))
                } else {
                    b.append(value)
                }
            }
        }
        return b.append("}").toString()
    }

    fun toSet(s: String, sep: String): Set<String> {
        return HashSet(Arrays.asList(*s.split(sep.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
    }

    @JvmOverloads
    fun tohex(data: ByteArray, start: Int = 0, len: Int = data.size): String {
        val b = StringBuilder()
        val stop = start + len
        for (i in start until stop) {
            b.append(getHexValue(data[i]))
        }
        return b.toString()
    }

    /**
     * Get the hexadecimal string representation for a byte.
     * The leading 0x is not included.
     *
     * @param b the byte to process
     * @return a String representing the hexadecimal value of the byte
     */
    fun getHexValue(b: Byte): String {
        val i = b.toInt()
        return MD5Util.HEX_DIGITS[((i shr 4) + 16) % 16] + MD5Util.HEX_DIGITS[(i + 128) % 16]
    }

    fun uncapitalize(s: String): String? {
        return if (empty(s)) s else if (s.length == 1) s.toLowerCase() else s.substring(0, 1).toLowerCase() + s.substring(1)
    }

    fun pluralize(`val`: String): String? {
        if (empty(`val`)) return `val`
        if (`val`.endsWith("y")) {
            return `val`.substring(0, `val`.length - 1) + "ies"
        } else if (!`val`.endsWith("s")) {
            return `val` + "s"
        }
        return `val`
    }

    fun exceptionContainsMessage(e: Throwable?, s: String): Boolean {
        return e != null && (e.message != null && e.message.contains(s) || e.cause != null && exceptionContainsMessage(e.cause, s))
    }

    fun ellipsis(s: String?, len: Int): String? {
        return if (s == null || s.length <= len) s else s.substring(0, len - 3) + "..."
    }

    fun truncate(s: String?, len: Int): String? {
        return if (s == null || s.length <= len) s else s.substring(0, len)
    }

    fun containsIgnoreCase(values: Collection<String>, value: String): Boolean {
        for (v in values) if (v != null && v.equals(value, ignoreCase = true)) return true
        return false
    }

    /**
     * Return what the default "property name" would be for this thing, if named according to its type
     * @param thing the thing to look at
     * @param <T> the type of thing it is
     * @return the class name of the thing with the first letter downcased
    </T> */
    fun <T> classAsFieldName(thing: T): String? {
        return uncapitalize(thing.javaClass.getSimpleName())
    }

    /**
     * Split a string into multiple query terms, respecting quotation marks
     * @param query The query string
     * @return a List of query terms
     */
    fun splitIntoTerms(query: String): List<String> {
        val terms = ArrayList<String>()
        val st = StringTokenizer(query, "\n\t \"", true)

        var current = StringBuilder()
        var inQuotes = false
        while (st.hasMoreTokens()) {
            val token = st.nextToken()
            if (token == "\"") {
                val term = current.toString().trim { it <= ' ' }
                if (term.length > 0) terms.add(term)
                current = StringBuilder()
                inQuotes = !inQuotes

            } else if (token.matches("\\s+".toRegex())) {
                if (inQuotes && !current.toString().endsWith(" ")) current.append(" ")

            } else {
                if (inQuotes) {
                    current.append(token)
                } else {
                    terms.add(token)
                }
            }
        }
        if (current.length > 0) terms.add(current.toString().trim { it <= ' ' })
        return terms
    }

    fun chop(input: String, chopIfSuffix: String): String {
        return if (input.endsWith(chopIfSuffix)) input.substring(0, input.length - chopIfSuffix.length) else input
    }

    fun isNumber(`val`: String?): Boolean {
        var `val`: String? = `val` ?: return false
        `val` = `val`!!.trim { it <= ' ' }
        try {
            java.lang.Double.parseDouble(`val`)
            return true
        } catch (ignored: Exception) {
        }

        try {
            java.lang.Long.parseLong(`val`)
            return true
        } catch (ignored: Exception) {
        }

        return false
    }

    fun isPunctuation(c: Char): Boolean {
        return c == '.' || c == ',' || c == '?' || c == '!' || c == ';' || c == ':'
    }

    fun hasScripting(value: String): Boolean {
        var value = value
        if (empty(value)) return false
        value = value.toLowerCase().replace("&lt;", "<")
        val nospace = removeWhitespace(value)
        return (nospace.contains("<script") || nospace.contains("javascript:")
                || nospace.contains("onfocus=") && value.contains(" onfocus")
                || nospace.contains("onblur=") && value.contains(" onblur")
                || nospace.contains("onload=") && value.contains(" onload")
                || nospace.contains("onunload=") && value.contains(" onunload")
                || nospace.contains("onselect=") && value.contains(" onselect")
                || nospace.contains("onchange=") && value.contains(" onchange")
                || nospace.contains("onmove=") && value.contains(" onmove")
                || nospace.contains("onreset=") && value.contains(" onreset")
                || nospace.contains("onresize=") && value.contains(" onresize")
                || nospace.contains("onclick=") && value.contains(" onclick")
                || nospace.contains("ondblclick=") && value.contains(" ondblclick")
                || nospace.contains("onmouseup=") && value.contains(" onmouseup")
                || nospace.contains("onmousedown=") && value.contains(" onmousedown")
                || nospace.contains("onmouseout=") && value.contains(" onmouseout")
                || nospace.contains("onmouseover=") && value.contains(" onmouseover")
                || nospace.contains("onmousemove=") && value.contains(" onmousemove")
                || nospace.contains("ondragdrop=") && value.contains(" ondragdrop")
                || nospace.contains("onkeyup=") && value.contains(" onkeyup")
                || nospace.contains("onkeydown=") && value.contains(" onkeydown")
                || nospace.contains("onkeypress=") && value.contains(" onkeypress")
                || nospace.contains("onsubmit=") && value.contains(" onsubmit")
                || nospace.contains("onerror=") && value.contains(" onerror"))
    }

    fun camelCaseToString(`val`: String): String? {
        if (empty(`val`)) return `val`
        val b = StringBuilder()
        b.append(Character.toUpperCase(`val`[0]))
        if (`val`.length == 1) return b.toString()
        for (i in 1 until `val`.length) {
            val c = `val`[i]
            if (Character.isUpperCase(c)) {
                b.append(' ')
            }
            b.append(c)
        }
        return b.toString()
    }

    fun snakeCaseToCamelCase(snake: String): String {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, snake)
    }

    fun camelCaseToSnakeCase(camel: String): String {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, camel)
    }

    fun formatCents(cents: Int): String {
        return "" + cents / 100 + if (cents % 100 == 0) "" else if (cents % 100 > 10) "." + cents % 100 else ".0" + cents % 100
    }

    fun hexPath(hex: String, count: Int): String {
        val b = StringBuilder()
        for (i in 0 until count) {
            if (b.length > 0) b.append("/")
            b.append(hex.substring(i * 2, i * 2 + 2))
        }
        return b.toString()
    }

    @JvmOverloads
    fun formatDollars(value: Long, sign: Boolean = true): String {
        return if (empty(value)) "" else (if (sign) "$" else "") + NUMBER_FORMAT.format(value)
    }

    fun formatDollarsWithSign(value: Long): String {
        return formatDollars(value, true)
    }

    fun formatDollarsNoSign(value: Long): String {
        return formatDollars(value, false)
    }

    fun formatDollarsAndCentsWithSign(value: Long): String {
        return formatDollarsAndCents(value, true)
    }

    fun formatDollarsAndCentsNoSign(value: Long): String {
        return formatDollarsAndCents(value, false)
    }

    @JvmOverloads
    fun formatDollarsAndCents(value: Long, sign: Boolean = true): String {
        return if (empty(value))
            ""
        else
            (if (sign) "$" else "") + NUMBER_FORMAT.format(value / 100)
                    + if (value % 100 == 0L) ".00" else "." + if (value % 100 < 10) "0" + value % 100 else value % 100
    }

    fun formatDollarsAndCentsPlain(value: Long): String {
        return "" + value / 100 + if (value % 100 == 0L) ".00" else "." + if (value % 100 < 10) "0" + value % 100 else value % 100
    }

    fun parseToCents(amount: String): Int {
        if (empty(amount)) return die("getDownAmountCents: downAmount was empty")
        var `val` = amount.trim { it <= ' ' }
        var dotPos = `val`.indexOf(".")
        if (dotPos == `val`.length) {
            `val` = `val`.substring(0, `val`.length - 1)
            dotPos = -1
        }
        return if (dotPos == -1) 100 * Integer.parseInt(`val`) else 100 * Integer.parseInt(`val`.substring(0, dotPos)) + Integer.parseInt(`val`.substring(dotPos + 1))
    }

    fun parsePercent(pct: String): Double {
        if (empty(pct)) die<Any>("parsePercent: $pct")
        return java.lang.Double.parseDouble(chop(removeWhitespace(pct), "%"))
    }

    fun stream(data: String): ReaderInputStream {
        return ReaderInputStream(StringReader(data), UTF8cs)
    }

    fun firstMatch(s: String, regex: String): String? {
        val p = Pattern.compile(regex)
        val m = p.matcher(s)
        return if (m.find()) m.group(0) else null
    }

    fun diff(text1: String, text2: String, opts: Map<String, String>?): String? {
        var opts = opts
        if (opts == null) opts = HashMap()
        val ctx = HashMap<String, Any>()
        ctx["text1"] = text1
        ctx["text2"] = text2
        ctx["opts"] = opts
        return DIFF_JS_ENGINE.evaluate<String>(DIFF_JS, ctx)
    }

    fun replaceWithRandom(s: String, find: String, randLength: Int): String {
        var s = s
        while (s.contains(find)) s = s.replaceFirst(find.toRegex(), randomAlphanumeric(randLength))
        return s
    }

    fun firstWord(value: String): String {
        return value.trim { it <= ' ' }.split("\\p{javaSpaceChar}+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
    }

    /**
     * If both strings are empty (null or empty string) return true, else use apache's StringUtils.equals method.
     */
    fun equalsExtended(s1: String, s2: String): Boolean {
        return empty(s1) && empty(s2) || StringUtils.equals(s1, s2)
    }

    fun sqlFilter(value: String): String {
        // escape any embedded '%' chars, and then add '%' as the first and last chars
        // also replace any embedded single-quote characters with '%', this helps prevent SQL injection attacks
        return PCT + value.toLowerCase().replace(PCT, ESC_PCT).replace("'", PCT) + PCT
    }
}

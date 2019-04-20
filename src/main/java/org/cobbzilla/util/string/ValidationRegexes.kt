package org.cobbzilla.util.string

import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern

import org.cobbzilla.util.string.StringUtil.chop

object ValidationRegexes {

    val LOGIN_PATTERN = pattern("^[\\w\\-]+$")
    val EMAIL_PATTERN = pattern("^[A-Z0-9][A-Z0-9._%+-]*@[A-Z0-9.-]+\\.[A-Z]{2,6}$")
    val EMAIL_NAME_PATTERN = pattern("^[A-Z0-9][A-Z0-9._%+-]*$")

    val LOCALE_PATTERNS = arrayOf(pattern("^[a-zA-Z]{2,3}([-_][a-zA-z]{2}(@[\\w]+)?)?"), // ubuntu style: en_US or just en
            pattern("^[a-zA-Z]{2,3}([-_][\\w]+)?"))// some apps use style: ca-valencia

    val UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
    val UUID_PATTERN = pattern(UUID_REGEX)

    val IPv4_PATTERN = pattern("^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)){3}$")
    val IPv6_PATTERN = pattern("^(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))$")

    val HOST_REGEX = "^([A-Z0-9]{1,63}|[A-Z0-9][A-Z0-9\\-]{0,61}[A-Z0-9])(\\.([A-Z0-9]{1,63}|[A-Z0-9][A-Z0-9\\-]{0,61}[A-Z0-9]))*$"
    val HOST_PATTERN = pattern(HOST_REGEX)
    val HOST_PART_PATTERN = pattern("^([A-Z0-9]|[A-Z0-9][A-Z0-9\\-]{0,61}[A-Z0-9])$")
    val PORT_PATTERN = pattern("^[\\d]{1,5}$")

    val DOMAIN_REGEX = "^([A-Z0-9]{1,63}|[A-Z0-9][A-Z0-9\\-]{0,61}[A-Z0-9])(\\.([A-Z0-9]{1,63}|[A-Z0-9][A-Z0-9\\-]{0,61}[A-Z0-9]))+$"
    val DOMAIN_PATTERN = pattern(DOMAIN_REGEX)

    val URL_REGEX = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"
    val URL_REGEX_ONLY = "^$URL_REGEX$"
    val URL_PATTERN = pattern(URL_REGEX_ONLY)
    val HTTP_PATTERN = pattern("^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]$")
    val HTTPS_PATTERN = pattern("^https://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]$")

    val VARNAME_REGEX = "^[A-Za-z][_A-Za-z0-9]+$"
    val VARNAME_PATTERN = pattern(VARNAME_REGEX)

    val FILENAME_PATTERN = pattern("^[_A-Z0-9\\-\\.]+$")
    val INTEGER_PATTERN = pattern("^[0-9]+$")
    val DECIMAL_PATTERN = pattern("^[0-9]+(\\.[0-9]+)?$")

    val YYYYMMDD_REGEX = "^(19|20|21)[0-9]{2}-[01][0-9]-(0[1-9]|[1-2][0-9]|3[0-1])$"
    val YYYYMMDD_PATTERN = pattern(YYYYMMDD_REGEX)

    val ZIPCODE_REGEX = "^\\d{5}(-\\d{4})?$"
    val ZIPCODE_PATTERN = pattern(ZIPCODE_REGEX)

    fun pattern(regex: String): Pattern {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
    }

    fun findAllRegexMatches(text: String, regex: String): List<String> {
        var regex = regex
        if (regex.startsWith("^")) regex = regex.substring(1)
        if (regex.endsWith("$")) regex = chop(regex, "$")
        regex = "(.*(?<match>$regex)+.*)+"
        val found = ArrayList<String>()
        val matcher = Pattern.compile(regex, Pattern.MULTILINE).matcher(text)
        while (matcher.find()) {
            found.add(matcher.group("match"))
        }
        return found
    }

}

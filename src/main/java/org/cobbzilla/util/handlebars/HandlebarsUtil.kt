package org.cobbzilla.util.handlebars

import com.fasterxml.jackson.core.io.JsonStringEncoder
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.HandlebarsException
import com.github.jknack.handlebars.Helper
import com.github.jknack.handlebars.Options
import com.github.jknack.handlebars.io.AbstractTemplateLoader
import com.github.jknack.handlebars.io.StringTemplateSource
import com.github.jknack.handlebars.io.TemplateSource
import lombok.AllArgsConstructor
import lombok.Cleanup
import lombok.Getter
import lombok.Setter
import lombok.extern.slf4j.Slf4j
import org.apache.commons.collections.iterators.ArrayIterator
import org.apache.commons.lang3.StringUtils
import org.apache.pdfbox.io.IOUtils
import org.cobbzilla.util.collection.SingletonList
import org.cobbzilla.util.http.HttpContentTypes
import org.cobbzilla.util.io.FileResolver
import org.cobbzilla.util.io.FileUtil
import org.cobbzilla.util.io.PathListFileResolver
import org.cobbzilla.util.javascript.JsEngineFactory
import org.cobbzilla.util.reflect.ReflectionUtil
import org.cobbzilla.util.string.LocaleUtil
import org.cobbzilla.util.string.StringUtil
import org.cobbzilla.util.time.TimeUtil
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Period
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.StringWriter
import java.lang.reflect.Method
import java.math.BigDecimal
import java.math.MathContext
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicReference

import java.util.regex.Pattern.quote
import org.cobbzilla.util.daemon.DaemonThreadFactory.fixedPool
import org.cobbzilla.util.daemon.ZillaRuntime.*
import org.cobbzilla.util.io.StreamUtil.loadResourceAsStream
import org.cobbzilla.util.io.StreamUtil.stream2string
import org.cobbzilla.util.json.JsonUtil.json
import org.cobbzilla.util.security.ShaUtil.sha256_hex
import org.cobbzilla.util.string.Base64.encodeBytes
import org.cobbzilla.util.string.Base64.encodeFromFile
import org.cobbzilla.util.string.StringUtil.*

@AllArgsConstructor
@Slf4j
class HandlebarsUtil : AbstractTemplateLoader {

    private val sourceName = "unknown"

    @Throws(IOException::class)
    override fun sourceAt(source: String): TemplateSource {
        return StringTemplateSource(sourceName, source)
    }

    private abstract class DateHelper : Helper<Any> {

        @JvmOverloads
        protected fun getTimeZone(options: Options, index: Int = 0): DateTimeZone {
            val timeZoneName = options.param(index, defaultTimeZone)
            try {
                return DateTimeZone.forID(timeZoneName)
            } catch (e: Exception) {
                return die("getTimeZone: invalid timezone: $timeZoneName")
            }

        }

        @JvmOverloads
        protected fun zonedTimestamp(src: Any, options: Options, index: Int = 0): Long {
            var src = src
            if (empty(src)) src = "now"
            val timeZone = getTimeZone(options, index)
            return longVal(src, timeZone)
        }

        protected fun print(formatter: DateTimeFormatter, src: Any, options: Options): CharSequence {
            return Handlebars.SafeString(formatter.print(DateTime(zonedTimestamp(src, options),
                    getTimeZone(options))))
        }
    }

    @AllArgsConstructor
    private class FileLoaderHelper : Helper<String> {

        private val isBase64EncoderOn: Boolean = false

        @Throws(IOException::class)
        override fun apply(filename: String, options: Options): CharSequence {
            if (empty(filename)) return EMPTY_SAFE_STRING

            val include = options.get("includePath", DEFAULT_FILE_RESOLVER)
            val fileResolver = fileResolverMap[include]
                    ?: return die("apply: no file resolve found for includePath=$include")

            val escapeSpecialChars = options.get("escape", false)

            var f: File? = fileResolver.resolve(filename)
            if (f == null && filename.startsWith(File.separator)) {
                // looks like an absolute path, try the filesystem
                f = File(filename)
                if (!f.exists() || !f.canRead()) f = null
            }

            if (f == null) {
                // try classpath
                try {
                    var content = if (isBase64EncoderOn)
                        encodeBytes(IOUtils.toByteArray(loadResourceAsStream(filename)))
                    else
                        stream2string(filename)
                    if (escapeSpecialChars) {
                        content = String(JsonStringEncoder.getInstance().quoteAsString(content))
                    }
                    return Handlebars.SafeString(content)
                } catch (e: Exception) {
                    throw FileNotFoundException("Cannot find readable file $filename, resolver: $fileResolver")
                }

            }

            try {
                var string: String? = if (isBase64EncoderOn) encodeFromFile(f) else FileUtil.toString(f)
                if (escapeSpecialChars) string = String(JsonStringEncoder.getInstance().quoteAsString(string!!))
                return Handlebars.SafeString(string)
            } catch (e: IOException) {
                return die("Cannot read file from: $f", e)
            }

        }
    }

    companion object {

        val HB_START_CHAR = '{'
        val HB_END_CHAR = '}'

        val HB_START = StringUtils.repeat(HB_START_CHAR, 2)
        val HB_END = StringUtils.repeat(HB_END_CHAR, 2)

        val HB_LSTART = StringUtils.repeat(HB_START_CHAR, 3)
        val HB_LEND = StringUtils.repeat(HB_END_CHAR, 3)

        val DEFAULT_FLOAT_FORMAT = "%1$,.3f"
        val JSON_STRING_ENCODER = JsonStringEncoder()

        @JvmOverloads
        fun apply(handlebars: Handlebars, map: Map<String, Any>, ctx: Map<String, Any>, altStart: Char = HB_START_CHAR, altEnd: Char = HB_END_CHAR): Map<String, Any>? {
            if (empty(map)) return map
            val merged = LinkedHashMap<String, Any>()
            val hbStart = StringUtils.repeat(altStart, 2)
            val hbEnd = StringUtils.repeat(altEnd, 2)
            for ((key, value) in map) {
                if (value is String) {
                    if (value.contains(hbStart) && value.contains(hbEnd)) {
                        merged[key] = apply(handlebars, value.toString(), ctx, altStart, altEnd)
                    } else {
                        merged[key] = value
                    }

                } else if (value is Map<*, *>) {
                    // recurse
                    merged[key] = apply(handlebars, value as Map<String, Any>, ctx, altStart, altEnd)

                } else {
                    log.info("apply: ")
                    merged[key] = value
                }
            }
            return merged
        }

        val DUMMY_START3 = "~~~___~~~"
        val DUMMY_START2 = "~~__~~"
        val DUMMY_END3 = "___~~~___"
        val DUMMY_END2 = "__~~__"
        @JvmOverloads
        fun apply(handlebars: Handlebars, value: String, ctx: Map<String, Any>, altStart: Char = 0.toChar(), altEnd: Char = 0.toChar()): String {
            var value = value
            if (altStart.toInt() != 0 && altEnd.toInt() != 0 && altStart != HB_START_CHAR && altEnd != HB_END_CHAR) {
                val s3 = StringUtils.repeat(altStart, 3)
                val s2 = StringUtils.repeat(altStart, 2)
                val e3 = StringUtils.repeat(altEnd, 3)
                val e2 = StringUtils.repeat(altEnd, 2)
                // escape existing handlebars delimiters with dummy placeholders (we'll put them back later)
                value = value.replace(quote(HB_LSTART).toRegex(), DUMMY_START3).replace(HB_LEND.toRegex(), DUMMY_END3)
                        .replace(quote(HB_START).toRegex(), DUMMY_START2).replace(HB_END.toRegex(), DUMMY_END2)
                        // replace our custom start/end delimiters with handlebars standard ones
                        .replace(quote(s3).toRegex(), HB_LSTART).replace(quote(e3).toRegex(), HB_LEND)
                        .replace(quote(s2).toRegex(), HB_START).replace(quote(e2).toRegex(), HB_END)
                // run handlebars, then put the real handlebars stuff back (removing the dummy placeholders)
                value = apply(handlebars, value, ctx)
                        .replace(DUMMY_START3.toRegex(), HB_LSTART).replace(DUMMY_END3.toRegex(), HB_LEND)
                        .replace(DUMMY_START2.toRegex(), HandlebarsUtil.HB_START).replace(DUMMY_END2.toRegex(), HB_END)
                return value
            }
            try {
                @Cleanup val writer = StringWriter(value.length)
                handlebars.compile(value).apply(ctx, writer)
                return writer.toString()
            } catch (e: HandlebarsException) {
                val cause = e.cause
                if (cause != null && (cause is FileNotFoundException || cause is RequiredVariableUndefinedException)) {
                    log.error(e.message + ": \"" + value + "\"")
                    throw e
                }
                return die("apply($value): $e", e)
            } catch (e: Exception) {
                return die("apply($value): $e", e)
            } catch (e: Error) {
                log.warn("apply: $e", e)
                throw e
            }

        }

        /**
         * Using reflection, we find all public getters of a thing (and if the getter returns an object, find all
         * of its public getters, recursively and so on). We limit our results to those getters that have corresponding
         * setters: methods whose sole parameter is of a compatible type with the return type of the getter.
         * For each such property whose value is a String, we apply handlebars using the provided context.
         * @param handlebars the handlebars template processor
         * @param thing the object to operate upon
         * @param ctx the context to apply
         * @param <T> the return type
         * @return the thing, possibly with String-valued properties having been modified
        </T> */
        fun <T> applyReflectively(handlebars: Handlebars, thing: T, ctx: Map<String, Any>): T {
            return applyReflectively(handlebars, thing, ctx, HB_START_CHAR, HB_END_CHAR)
        }

        fun <T> applyReflectively(handlebars: Handlebars, thing: T, ctx: Map<String, Any>, altStart: Char, altEnd: Char): T {
            for (getterCandidate in thing.javaClass.getMethods()) {

                if (!getterCandidate.name.startsWith("get")) continue
                if (!canApplyReflectively(getterCandidate.returnType)) continue

                val setterName = ReflectionUtil.setterForGetter(getterCandidate.name)
                for (setterCandidate in thing.javaClass.getMethods()) {
                    if (setterCandidate.name != setterName
                            || setterCandidate.parameterTypes.size != 1
                            || !setterCandidate.parameterTypes[0].isAssignableFrom(getterCandidate.returnType)) {
                        continue
                    }
                    try {
                        val value = getterCandidate.invoke(thing, *null as Array<Any>?) ?: break
                        if (value is String) {
                            if (value.toString().contains("" + altStart + altStart)) {
                                setterCandidate.invoke(thing, apply(handlebars, value, ctx, altStart, altEnd))
                            }

                        } else if (value is JsonNode) {
                            setterCandidate.invoke(thing, json(apply(handlebars, json(value), ctx, altStart, altEnd), JsonNode::class.java))

                        } else if (value is Map<*, *>) {
                            setterCandidate.invoke(thing, apply(handlebars, value as Map<String, Any>, ctx, altStart, altEnd))

                            //                    } else if (Object[].class.isAssignableFrom(value.getClass())) {
                            //                        final Object[] array = (Object[]) value;
                            //                        final Object[] rendered = new Object[array.length];
                            //                        for (int i=0; i<array.length; i++) {
                            //                            rendered[i] = applyReflectively(handlebars, array[i], ctx, altStart, altEnd);
                            //                        }
                            //                        try {
                            //                            setterCandidate.invoke(thing, rendered);
                            //                        } catch (Exception e) {
                            //                            die(e);
                            //                        }
                            //
                        } else {
                            // recurse
                            setterCandidate.invoke(thing, applyReflectively(handlebars, value, ctx, altStart, altEnd))
                        }
                    } catch (e: HandlebarsException) {
                        throw e

                    } catch (e: Exception) {
                        // no setter for getter
                        log.warn("applyReflectively: $e")
                    }

                }
            }
            return thing
        }

        private fun canApplyReflectively(returnType: Class<*>): Boolean {
            if (returnType == String::class.java) return true
            try {
                return !(returnType.isPrimitive || returnType.getPackage() != null && returnType.getPackage().name == "java.lang")
            } catch (npe: NullPointerException) {
                log.warn("canApplyReflectively($returnType): $npe")
                return false
            }

        }

        val EMPTY_SAFE_STRING: CharSequence = ""

        private val messageSender = AtomicReference<ContextMessageSender>()

        fun setMessageSender(sender: ContextMessageSender) {
            synchronized(messageSender) {
                val current = messageSender.get()
                if (current != null && current !== sender && current != sender) die<Any>("setMessageSender: already set to $current")
                messageSender.set(sender)
            }
        }

        fun registerUtilityHelpers(hb: Handlebars) {
            hb.registerHelper<Any>("exists") { src, options -> if (empty(src)) null else options.apply(options.fn) }

            hb.registerHelper<Any>("not_exists") { src, options -> if (!empty(src)) null else options.apply(options.fn) }

            hb.registerHelper<Any>("sha256") { src, options ->
                if (empty(src)) return@hb.registerHelper ""
                src = apply(hb, src.toString(), options.context.model() as Map<String, Any>)
                src = sha256_hex(src.toString())
                Handlebars.SafeString(src.toString())
            }

            hb.registerHelper<Any>("format_float") { `val`, options ->
                if (empty(`val`)) return@hb.registerHelper ""
                if (options.params.size > 2) return@hb.registerHelper die < Any >("format_float: too many parameters. Usage: {{format_float expr [format] [locale]}}")
                val format = if (options.params.size > 0 && !empty(options.param(0))) options.param(0) else DEFAULT_FLOAT_FORMAT
                val locale = LocaleUtil.fromString(if (options.params.size > 1 && !empty(options.param(1))) options.param<String>(1) else null)

                `val` = apply(hb, `val`.toString(), options.context.model() as Map<String, Any>)
                `val` = String.format(locale, format, java.lang.Double.valueOf(`val`.toString()))
                Handlebars.SafeString(`val`.toString())
            }

            hb.registerHelper<Any>("json") { src, options ->
                if (empty(src)) return@hb.registerHelper ""
                Handlebars.SafeString(json(src))
            }

            hb.registerHelper<Any>("escaped_json") { src, options ->
                if (empty(src)) return@hb.registerHelper ""
                Handlebars.SafeString(String(JSON_STRING_ENCODER.quoteAsString(json(src))))
            }

            hb.registerHelper<Any>("context") { src, options ->
                if (empty(src)) return@hb.registerHelper ""
                if (options.params.size > 0) return@hb.registerHelper die < Any >("context: too many parameters. Usage: {{context [recipient]}}")
                val ctxString = options.context.toString()
                val recipient = src.toString()
                val subject = if (options.params.size > 1) options.param<String>(0) else null
                sendContext(recipient, subject, ctxString, HttpContentTypes.TEXT_PLAIN)
                Handlebars.SafeString(ctxString)
            }

            hb.registerHelper<Any>("context_json") { src, options ->
                if (empty(src)) return@hb.registerHelper ""
                try {
                    if (options.params.size > 0) return@hb.registerHelper die < Any >("context: too many parameters. Usage: {{context [recipient]}}")
                    val json = json(options.context.model())
                    val recipient = src.toString()
                    val subject = if (options.params.size > 1) options.param<String>(0) else null
                    sendContext(recipient, subject, json, HttpContentTypes.APPLICATION_JSON)
                    return@hb.registerHelper Handlebars . SafeString json
                } catch (e: Exception) {
                    return@hb.registerHelper Handlebars . SafeString "Error calling json(options.context): " + e.javaClass + ": " + e.message
                }
            }

            hb.registerHelper<Any>("required") { src, options ->
                if (src == null) throw RequiredVariableUndefinedException("required: undefined variable")
                Handlebars.SafeString(src.toString())
            }

            hb.registerHelper<Any>("safe_name") { src, options ->
                if (empty(src)) return@hb.registerHelper ""
                Handlebars.SafeString(safeSnakeName(src.toString()))
            }

            hb.registerHelper<Any>("urlEncode") { src, options ->
                if (empty(src)) return@hb.registerHelper ""
                src = apply(hb, src.toString(), options.context.model() as Map<String, Any>)
                src = urlEncode(src.toString())
                Handlebars.SafeString(src.toString())
            }

            hb.registerHelper<Any>("lastElement") { thing, options ->
                if (thing == null) return@hb.registerHelper null
                val iter = getIterator(thing)
                val path = options.param<String>(0)
                var lastElement: Any? = null
                while (iter.hasNext()) {
                    lastElement = iter.next()
                }
                val `val` = ReflectionUtil.get(lastElement, path)
                if (`val` != null) return@hb.registerHelper Handlebars . SafeString "" + `val`
                EMPTY_SAFE_STRING
            }

            hb.registerHelper<Any>("find") { thing, options ->
                if (thing == null) return@hb.registerHelper null
                val iter = getIterator(thing)
                val path = options.param<String>(0)
                val arg = options.param<String>(1)
                val output = options.param<String>(2)
                while (iter.hasNext()) {
                    val item = iter.next()
                    try {
                        val `val` = ReflectionUtil.get(item, path)
                        if (`val` != null && `val`.toString() == arg) {
                            return@hb.registerHelper Handlebars . SafeString "" + ReflectionUtil.get(item, output)!!
                        }
                    } catch (e: Exception) {
                        log.warn("find: $e")
                    }

                }
                EMPTY_SAFE_STRING
            }

            hb.registerHelper<Any>("compare") { val1, options ->
                val operator = options.param<String>(0)
                val val2 = getComparisonArgParam(options)
                val v1 = cval(val1)
                val v2 = cval(val2)
                if (v1 == null && v2 == null || v1 != null && compare<Any>(operator, v1, v2)) options.fn(options) else options.inverse(options)
            }

            hb.registerHelper<Any>("string_compare") { val1, options ->
                val operator = options.param<String>(0)
                val val2 = getComparisonArgParam(options)
                val v1 = val1?.toString()
                val v2 = val2?.toString()
                if (compare(operator, v1, v2)) options.fn(options) else options.inverse(options)
            }

            hb.registerHelper<Any>("long_compare") { val1, options ->
                val operator = options.param<String>(0)
                val val2 = getComparisonArgParam(options)
                val v1 = if (val1 == null) null else java.lang.Long.valueOf(val1.toString())
                val v2 = if (val2 == null) null else java.lang.Long.valueOf(val2.toString())
                if (compare(operator, v1, v2)) options.fn(options) else options.inverse(options)
            }

            hb.registerHelper<Any>("double_compare") { val1, options ->
                val operator = options.param<String>(0)
                val val2 = getComparisonArgParam(options)
                val v1 = if (val1 == null) null else java.lang.Double.valueOf(val1.toString())
                val v2 = if (val2 == null) null else java.lang.Double.valueOf(val2.toString())
                if (compare(operator, v1, v2)) options.fn(options) else options.inverse(options)
            }

            hb.registerHelper<Any>("big_compare") { val1, options ->
                val operator = options.param<String>(0)
                val val2 = getComparisonArgParam(options)
                val v1 = if (val1 == null) null else big(val1.toString())
                val v2 = if (val2 == null) null else big(val2.toString())
                if (compare(operator, v1, v2)) options.fn(options) else options.inverse(options)
            }

            hb.registerHelper<Any>("expr") { val1, options ->
                val operator = options.param<String>(0)
                val format = if (options.params.size > 2) options.param<String>(2) else null
                val val2 = getComparisonArgParam(options)
                val v1 = val1.toString()
                val v2 = val2!!.toString()

                val result: BigDecimal
                when (operator) {
                    "+" -> result = big(v1).add(big(v2))
                    "-" -> result = big(v1).subtract(big(v2))
                    "*" -> result = big(v1).multiply(big(v2))
                    "/" -> result = big(v1).divide(big(v2), MathContext.DECIMAL128)
                    "%" -> result = big(v1).remainder(big(v2)).abs()
                    "^" -> result = big(v1).pow(big(v2).toInt())
                    else -> return@hb.registerHelper die < Any >("expr: invalid operator: $operator")
                }

                // can't use trigraph (?:) operator here, if we do then for some reason rval always ends up as a double
                val rval: Number
                if (v1.contains(".") || v2.contains(".") || operator == "/") {
                    rval = result.toDouble()
                } else {
                    rval = result.toInt()
                }
                if (format != null) {
                    val locale = LocaleUtil.fromString(if (options.params.size > 3 && !empty(options.param(3))) options.param<String>(3) else null)
                    return@hb.registerHelper Handlebars . SafeString String.format(locale, format, rval)
                } else {
                    return@hb.registerHelper Handlebars . SafeString rval.toString()
                }
            }

            hb.registerHelper("truncate", { max, options ->
                val `val` = options.param(0, " ")
                if (empty(`val`)) return@hb.registerHelper ""
                if (max == -1 || max >= `val`.length) return@hb.registerHelper `val`
                        Handlebars.SafeString(`val`.substring(0, max!!))
            } as Helper<Int>)

            hb.registerHelper("truncate_and_url_encode", { max, options ->
                val `val` = options.param(0, " ")
                if (empty(`val`)) return@hb.registerHelper ""
                if (max == -1 || max >= `val`.length) return@hb.registerHelper simpleUrlEncode `val`
                Handlebars.SafeString(simpleUrlEncode(`val`.substring(0, max!!)))
            } as Helper<Int>)

            hb.registerHelper("truncate_and_double_url_encode", { max, options ->
                val `val` = options.param(0, " ")
                if (empty(`val`)) return@hb.registerHelper ""
                if (max == -1 || max >= `val`.length) return@hb.registerHelper simpleUrlEncode simpleUrlEncode(`val`)
                Handlebars.SafeString(simpleUrlEncode(simpleUrlEncode(`val`.substring(0, max!!))))
            } as Helper<Int>)

            hb.registerHelper<Any>("length") { thing, options ->
                if (empty(thing)) return@hb.registerHelper "0"
                if (thing.javaClass.isArray) return@hb.registerHelper ""+(thing as Array<Any>).size
                if (thing is Collection<*>) return@hb.registerHelper ""+(thing as Collection<*>).size
                if (thing is ArrayNode) return@hb.registerHelper ""+(thing as ArrayNode).size()
                ""
            }

            hb.registerHelper<Any>("first_nonempty") { thing, options ->
                if (!empty(thing)) return@hb.registerHelper Handlebars . SafeString thing.toString()
                for (param in options.params) {
                    if (!empty(param)) return@hb.registerHelper Handlebars . SafeString param.toString()
                }
                EMPTY_SAFE_STRING
            }
        }

        fun getComparisonArgParam(options: Options): Any? {
            return if (options.params.size <= 1) die("getComparisonArgParam: missing argument") else options.param<Any>(1)
        }

        fun getEmailRecipient(hb: Handlebars, options: Options, index: Int): String? {
            return if (options.params.size > index && !empty(options.param(index)))
                apply(hb, options.param<Any>(index).toString(), options.context.model() as Map<String, Any>)
            else
                null
        }

        private val contextSender = fixedPool(10)

        fun sendContext(recipient: String, subject: String?, message: String, contentType: String) {
            contextSender.submit {
                if (!empty(recipient) && !empty(message)) {
                    synchronized(messageSender) {
                        val sender = messageSender.get()
                        if (sender != null) {
                            try {
                                sender.send(recipient, subject, message, contentType)
                            } catch (e: Exception) {
                                log.error("context: error sending message: $e", e)
                            }

                        }
                    }
                }
            }
        }

        private fun getIterator(thing: Any?): Iterator<*> {
            return (thing as? Collection<*>)?.iterator() ?: ((thing as? Map<*, *>)?.values?.iterator()
                    ?: if (Array<Any>::class.java.isAssignableFrom(thing!!.javaClass)) {
                        ArrayIterator(thing)
                    } else {
                        die("find: invalid argument type " + thing.javaClass.name)
                    })
        }

        private fun cval(v: Any?): Comparable<*>? {
            if (v == null) return null
            if (v is Number) return v as Comparable<*>?
            if (v is String) {
                val s = v.toString()
                try {
                    return java.lang.Long.parseLong(s)
                } catch (e: Exception) {
                    try {
                        return big(s)
                    } catch (e2: Exception) {
                        return s
                    }

                }

            } else {
                return die<Comparable<*>>("don't know to compare objects of class " + v.javaClass)
            }
        }

        fun <T> compare(operator: String, v1: Comparable<T>?, v2: T?): Boolean {
            if (v1 == null) return v2 == null
            if (v2 == null) return false
            val result: Boolean
            val parts: List<String>
            when (operator) {
                "==" -> result = v1 == v2
                "!=" -> result = v1 != v2
                ">" -> result = v1.compareTo(v2) > 0
                ">=" -> result = v1.compareTo(v2) >= 0
                "<" -> result = v1.compareTo(v2) < 0
                "<=" -> result = v1.compareTo(v2) <= 0
                "in" -> {
                    parts = StringUtil.split(v2.toString(), ", \n\t")
                    for (part in parts) {
                        if (v1 == part) return true
                    }
                    return false
                }
                "not_in" -> {
                    parts = StringUtil.split(v2.toString(), ", \n\t")
                    for (part in parts) {
                        if (v1 == part) return false
                    }
                    return true
                }
                else -> result = false
            }
            return result
        }

        fun registerCurrencyHelpers(hb: Handlebars) {
            hb.registerHelper<Any>("dollarsNoSign") { src, options ->
                if (empty(src)) return@hb.registerHelper ""
                Handlebars.SafeString(formatDollarsNoSign(longDollarVal(src)))
            }

            hb.registerHelper<Any>("dollarsWithSign") { src, options ->
                if (empty(src)) return@hb.registerHelper ""
                Handlebars.SafeString(formatDollarsWithSign(longDollarVal(src)))
            }

            hb.registerHelper<Any>("dollarsAndCentsNoSign") { src, options ->
                if (empty(src)) return@hb.registerHelper ""
                Handlebars.SafeString(formatDollarsAndCentsNoSign(longDollarVal(src)))
            }

            hb.registerHelper<Any>("dollarsAndCentsWithSign") { src, options ->
                if (empty(src)) return@hb.registerHelper ""
                Handlebars.SafeString(formatDollarsAndCentsWithSign(longDollarVal(src)))
            }

            hb.registerHelper<Any>("dollarsAndCentsPlain") { src, options ->
                if (empty(src)) return@hb.registerHelper ""
                Handlebars.SafeString(formatDollarsAndCentsPlain(longDollarVal(src)))
            }
        }

        @Getter
        @Setter
        var defaultTimeZone = "US/Eastern"
            set(defaultTimeZone) {
                field = this.defaultTimeZone
            }

        fun registerDateHelpers(hb: Handlebars) {

            hb.registerHelper("date_format", object : DateHelper() {
                override fun apply(src: Any, options: Options): CharSequence {
                    val formatter = DateTimeFormat.forPattern(options.param(0))
                    return Handlebars.SafeString(formatter.print(DateTime(zonedTimestamp(src, options, 1),
                            getTimeZone(options, 1))))
                }
            })

            hb.registerHelper("date_short", object : DateHelper() {
                override fun apply(src: Any, options: Options): CharSequence {
                    return print(TimeUtil.DATE_FORMAT_MMDDYYYY, src, options)
                }
            })

            hb.registerHelper("date_yyyy_mm_dd", object : DateHelper() {
                override fun apply(src: Any, options: Options): CharSequence {
                    return print(TimeUtil.DATE_FORMAT_YYYY_MM_DD, src, options)
                }
            })

            hb.registerHelper("date_mmm_dd_yyyy", object : DateHelper() {
                override fun apply(src: Any, options: Options): CharSequence {
                    return print(TimeUtil.DATE_FORMAT_MMM_DD_YYYY, src, options)
                }
            })

            hb.registerHelper("date_long", object : DateHelper() {
                override fun apply(src: Any, options: Options): CharSequence {
                    return print(TimeUtil.DATE_FORMAT_MMMM_D_YYYY, src, options)
                }
            })

            hb.registerHelper("timestamp", object : DateHelper() {
                override fun apply(src: Any, options: Options): CharSequence {
                    return Handlebars.SafeString(java.lang.Long.toString(zonedTimestamp(src, options)))
                }
            })
        }

        private fun longVal(src: Any?, timeZone: DateTimeZone, tryAgain: Boolean = true): Long {
            if (src == null) return now()
            val srcStr = src.toString().trim { it <= ' ' }

            if (srcStr == "" || srcStr == "0" || srcStr == "now") return now()

            if (srcStr.startsWith("now")) {
                // Multiple periods may be added to the original timestamp (separated by comma), but in the correct order.
                val splitSrc = srcStr.substring(3).split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                var result = DateTime(now(), timeZone).withTimeAtStartOfDay()
                for (period in splitSrc) {
                    result = result.plus(Period.parse(period, TimeUtil.PERIOD_FORMATTER))
                }
                return result.millis
            }

            try {
                return (src as Number).toLong()
            } catch (e: Exception) {
                if (!tryAgain) return die("longVal: unparseable long: " + src + ": " + e.javaClass.simpleName + ": " + e.message)

                // try to parse it in different formats
                val t = TimeUtil.parse(src.toString(), timeZone)
                return longVal(t, timeZone, false)
            }

        }

        fun longDollarVal(src: Any): Long {
            val `val` = ReflectionUtil.toLong(src)
            return `val` ?: 0
        }

        val CLOSE_XML_DECL = "?>"

        fun registerXmlHelpers(hb: Handlebars) {
            hb.registerHelper<Any>("strip_xml_declaration") { src, options ->
                if (empty(src)) return@hb.registerHelper ""
                var xml = src.toString().trim { it <= ' ' }
                if (xml.startsWith("<?xml")) {
                    val closeDecl = xml.indexOf(CLOSE_XML_DECL)
                    if (closeDecl != -1) {
                        xml = xml.substring(closeDecl + CLOSE_XML_DECL.length).trim { it <= ' ' }
                    }
                }
                Handlebars.SafeString(xml)
            }
        }

        fun registerJurisdictionHelpers(hb: Handlebars, jurisdictionResolver: JurisdictionResolver) {
            hb.registerHelper<Any>("us_state") { src, options ->
                if (empty(src)) return@hb.registerHelper ""
                Handlebars.SafeString(jurisdictionResolver.usState(src.toString()))
            }
            hb.registerHelper<Any>("us_zip") { src, options ->
                if (empty(src)) return@hb.registerHelper ""
                Handlebars.SafeString(jurisdictionResolver.usZip(src.toString()))
            }
        }

        fun registerJavaScriptHelper(hb: Handlebars, jsEngineFactory: JsEngineFactory) {
            hb.registerHelper<Any>("js") { src, options ->
                if (empty(src)) return@hb.registerHelper ""

                val format = if (options.params.size > 0 && !empty(options.param(0))) options.param<String>(0) else null
                val locale = LocaleUtil.fromString(if (options.params.size > 1 && !empty(options.param(1))) options.param<String>(1) else null)

                val ctx = options.context.model() as Map<String, Any>
                val result = jsEngineFactory.js.evaluate<Any>(src.toString(), ctx)
                if (result == null) return@hb.registerHelper Handlebars . SafeString "null"
                if (format != null)
                    Handlebars.SafeString(String.format(locale, format, java.lang.Double.valueOf(result!!.toString())))
                else
                    Handlebars.SafeString(result!!.toString())
            }
        }

        val DEFAULT_FILE_RESOLVER = "_"
        private val fileResolverMap = HashMap<String, FileResolver>()

        fun setFileIncludePath(path: String) {
            setFileIncludePaths(DEFAULT_FILE_RESOLVER, SingletonList(path))
        }

        fun setFileIncludePaths(paths: Collection<String>) {
            setFileIncludePaths(DEFAULT_FILE_RESOLVER, paths)
        }

        fun setFileIncludePaths(name: String, paths: Collection<String>) {
            fileResolverMap[name] = PathListFileResolver(paths)
        }

        fun registerFileHelpers(hb: Handlebars) {
            hb.registerHelper<Any>("rawImagePng") { src, options ->
                if (empty(src)) return@hb.registerHelper ""

                val include = options.get("includePath", DEFAULT_FILE_RESOLVER)
                val fileResolver = fileResolverMap[include]
                if (fileResolver == null) return@hb.registerHelper die < Any >("rawImagePng: no file resolve found for includePath=$include")

                val f = fileResolver!!.resolve(src.toString())
                val imgSrc = if (f == null) src.toString() else f.absolutePath

                val width = options.get<Any>("width")
                val widthAttr = if (empty(width)) "" else "width=\"$width\" "
                Handlebars.SafeString(
                        "<img " + widthAttr + "src=\"data:image/png;base64," + imgSrc + "\"/>")
            }

            hb.registerHelper("base64File", FileLoaderHelper(true))
            hb.registerHelper("textFile", FileLoaderHelper(false))
        }
    }
}

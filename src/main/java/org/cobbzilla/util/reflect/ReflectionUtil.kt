package org.cobbzilla.util.reflect

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.commons.beanutils.MethodUtils
import org.apache.commons.collections.Transformer
import org.apache.commons.lang3.ArrayUtils
import org.slf4j.Logger

import java.io.Closeable
import java.lang.reflect.*
import java.math.BigDecimal
import java.util.*

import com.google.common.base.Preconditions.checkNotNull
import org.cobbzilla.util.collection.ArrayUtil.arrayToString
import org.cobbzilla.util.daemon.ZillaRuntime.*
import org.cobbzilla.util.string.StringUtil.uncapitalize

/**
 * Handy tools for working quickly with reflection APIs, which tend to be verbose.
 */
object ReflectionUtil {

    val EMPTY_CLASS_ARRAY = arrayOfNulls<Class<*>>(0)
    private val log = org.slf4j.LoggerFactory.getLogger(ReflectionUtil::class.java)

    val TO_MAP_STANDARD_EXCLUDES = arrayOf("declaringClass", "class")
    private val callerInspector = CallerInspector()

    fun toBoolean(`object`: Any?): Boolean? {
        if (`object` == null) return null
        if (`object` is Boolean) return `object`
        return if (`object` is String) java.lang.Boolean.valueOf(`object`.toString()) else null
    }

    fun toBoolean(`object`: Any, field: String, defaultValue: Boolean): Boolean? {
        val `val` = toBoolean(get(`object`, field))
        return `val` ?: defaultValue
    }

    fun toLong(`object`: Any?): Long? {
        if (`object` == null) return null
        if (`object` is Number) return `object`.toLong()
        return if (`object` is String) java.lang.Long.valueOf(`object`.toString()) else null
    }

    fun toInteger(`object`: Any?): Int? {
        if (`object` == null) return null
        if (`object` is Number) return `object`.toInt()
        return if (`object` is String) Integer.valueOf(`object`.toString()) else null
    }

    fun toIntegerOrNull(`object`: Any?): Int? {
        if (`object` == null) return null
        if (`object` is Number) return `object`.toInt()
        if (`object` is String) {
            try {
                return Integer.valueOf(`object`.toString())
            } catch (e: Exception) {
                log.info("toIntegerOrNull($`object`): $e")
                return null
            }

        }
        return null
    }

    fun toShort(`object`: Any?): Short? {
        if (`object` == null) return null
        if (`object` is Number) return `object`.toShort()
        return if (`object` is String) java.lang.Short.valueOf(`object`.toString()) else null
    }

    fun toFloat(`object`: Any?): Float? {
        if (`object` == null) return null
        if (`object` is Number) return `object`.toFloat()
        return if (`object` is String) java.lang.Float.valueOf(`object`.toString()) else null
    }

    fun toDouble(`object`: Any?): Double? {
        if (`object` == null) return null
        if (`object` is Number) return `object`.toDouble()
        return if (`object` is String) java.lang.Double.valueOf(`object`.toString()) else null
    }

    fun toBigDecimal(`object`: Any?): BigDecimal? {
        if (`object` == null) return null
        if (`object` is Double) return big((`object` as Double?)!!)
        if (`object` is Float) return big((`object` as Float?)!!)
        if (`object` is Number) return big(`object`.toLong())
        return if (`object` is String) big(`object`.toString()) else null
    }

    /**
     * Do a Class.forName and only throw unchecked exceptions.
     * @param clazz full class name. May end in [] to indicate array class
     * @param <T> The class type
     * @return A Class&lt;clazz&gt; object
    </T> */
    fun <T> forName(clazz: String): Class<out T> {
        if (empty(clazz)) return Any::class.java as Class<out T>
        if (clazz.endsWith("[]")) return arrayClass(forName<Any>(clazz.substring(0, clazz.length - 2)))
        try {
            return Class.forName(clazz) as Class<out T>
        } catch (e: Exception) {
            return die("Class.forName($clazz) error: $e", e)
        }

    }

    fun forNames(classNames: Array<String>): Collection<Class<*>> {
        val list = ArrayList<Class<*>>()
        if (!empty(classNames)) for (c in classNames) list.add(forName<Any>(c))
        return list
    }

    fun <T> arrayClass(clazz: Class<*>): Class<out T> {
        return forName("[L" + clazz.name + ";")
    }

    /**
     * Create an instance of a class, only throwing unchecked exceptions. The class must have a default constructor.
     * @param clazz we will instantiate an object of this type
     * @param <T> The class type
     * @return An Object that is an instance of Class&lt;clazz&gt; object
    </T> */
    fun <T> instantiate(clazz: Class<T>): T {
        try {
            return clazz.newInstance()
        } catch (e: Exception) {
            return die("Error instantiating $clazz: $e", e)
        }

    }

    /**
     * Create an instance of a class based on a class name, only throwing unchecked exceptions. The class must have a default constructor.
     * @param clazz full class name
     * @param <T> The class type
     * @return An Object that is an instance of Class&lt;clazz&gt; object
    </T> */
    fun <T> instantiate(clazz: String): T {
        try {
            return instantiate<Any>(forName<Any>(clazz)) as T
        } catch (e: Exception) {
            return die("instantiate($clazz): $e", e)
        }

    }

    /**
     * Create an instance of a class using the supplied argument to a matching single-argument constructor.
     * @param clazz The class to instantiate
     * @param argument The object that will be passed to a matching single-argument constructor
     * @param <T> Could be anything
     * @return A new instance of clazz, created using a constructor that matched argument's class.
    </T> */
    fun <T> instantiate(clazz: Class<T>, argument: Any): T {
        var constructor: Constructor<T>? = null
        var tryClass: Class<*> = argument.javaClass
        if (clazz.isPrimitive) {
            when (clazz.name) {
                "boolean" -> return java.lang.Boolean.valueOf(argument.toString()) as T
                "byte" -> return java.lang.Byte.valueOf(argument.toString()) as T
                "short" -> return java.lang.Short.valueOf(argument.toString()) as T
                "char" -> return Character.valueOf(if (empty(argument)) 0 else argument.toString()[0]) as T
                "int" -> return Integer.valueOf(argument.toString()) as T
                "long" -> return java.lang.Long.valueOf(argument.toString()) as T
                "float" -> return java.lang.Float.valueOf(argument.toString()) as T
                "double" -> return java.lang.Double.valueOf(argument.toString()) as T
                else -> return die("instantiate: unrecognized primitive type: " + clazz.name)
            }
        }
        while (constructor == null) {
            try {
                constructor = clazz.getConstructor(tryClass)
            } catch (e: NoSuchMethodException) {
                if (tryClass == Any::class.java) {
                    // try interfaces
                    for (iface in argument.javaClass.interfaces) {
                        try {
                            constructor = clazz.getConstructor(iface)
                        } catch (e2: NoSuchMethodException) {
                            // noop
                        }

                    }
                    break
                } else {
                    tryClass = tryClass.superclass
                }
            }

        }
        if (constructor == null) {
            die<Any>("instantiate: no constructor could be found for class " + clazz.name + ", argument type " + argument.javaClass.name)
        }
        try {
            return constructor!!.newInstance(argument)
        } catch (e: Exception) {
            return die("instantiate(" + clazz.name + ", " + argument + "): " + e, e)
        }

    }

    /**
     * Create an instance of a class using the supplied argument to a matching single-argument constructor.
     * @param clazz The class to instantiate
     * @param arguments The objects that will be passed to a matching constructor
     * @param <T> Could be anything
     * @return A new instance of clazz, created using a constructor that matched argument's class.
    </T> */
    fun <T> instantiate(clazz: Class<T>, vararg arguments: Any): T {
        try {
            for (constructor in clazz.constructors) {
                val cParams = constructor.parameterTypes
                if (cParams.size == arguments.size) {
                    var match = true
                    for (i in cParams.indices) {
                        if (!cParams[i].isAssignableFrom(arguments[i].javaClass)) {
                            match = false
                            break
                        }
                    }
                    if (match) return constructor.newInstance(*arguments) as T
                }
            }
            log.warn("instantiate(" + clazz.name + "): no matching constructor found, trying with exact match (will probably fail), args=" + ArrayUtils.toString(arguments))

            val parameterTypes = arrayOfNulls<Class<*>>(arguments.size)
            for (i in arguments.indices) {
                parameterTypes[i] = getSimpleClass(arguments[i])
            }
            return clazz.getConstructor(*parameterTypes).newInstance(*arguments)

        } catch (e: Exception) {
            return die("instantiate(" + clazz.name + ", " + Arrays.toString(arguments) + "): " + e, e)
        }

    }

    fun getSimpleClass(argument: Any): Class<*> {
        var argClass: Class<*> = argument.javaClass
        val enhancePos = argClass.name.indexOf("$\$Enhance")
        if (enhancePos != -1) {
            argClass = forName<Any>(argClass.name.substring(0, enhancePos))
        }
        return argClass
    }

    fun getSimpleClassName(argument: Any): String {
        return getSimpleClass(argument).javaClass.simpleName
    }

    /**
     * Make a copy of the object, assuming its class has a copy constructor
     * @param thing The thing to copy
     * @param <T> Whatevs
     * @return A copy of the object, created using the thing's copy constructor
    </T> */
    fun <T> copy(thing: T): T {
        return instantiate<out Any>(thing.javaClass, thing) as T
    }

    /**
     * Mirror the object. Create a new instance and copy all fields
     * @param thing The thing to copy
     * @param <T> Whatevs
     * @return A mirror of the object, created using the thing's default constructor and copying all fields with 'copy'
    </T> */
    fun <T> mirror(thing: T): T {
        val copy = instantiate<out Any>(thing.javaClass) as T
        copy(copy, thing)
        return copy
    }

    fun invokeStatic(m: Method, vararg values: Any): Any {
        try {
            return m.invoke(null, *values)
        } catch (e: Exception) {
            return die("invokeStatic: " + m.javaClass.simpleName + "." + m.name + "(" + arrayToString(values, ", ") + "): " + e, e)
        }

    }

    fun getDeclaredField(clazz: Class<*>, field: String): Field? {
        try {
            return clazz.getDeclaredField(field)
        } catch (e: NoSuchFieldException) {
            if (clazz == Any::class.java) {
                log.info("getDeclaredField: field not found " + clazz.name + "/" + field)
                return null
            }
        }

        return getDeclaredField(clazz.superclass, field)
    }

    fun getField(clazz: Class<*>, field: String): Field? {
        try {
            return clazz.getField(field)
        } catch (e: NoSuchFieldException) {
            if (clazz == Any::class.java) {
                log.info("getField: field not found " + clazz.name + "/" + field)
                return null
            }
        }

        return getDeclaredField(clazz.superclass, field)
    }

    fun <T> factoryMethod(clazz: Class<T>, value: Any): Method? {
        // find a static method that takes the value and returns an instance of the class
        for (m in clazz.methods) {
            if (m.returnType == clazz) {
                val parameterTypes = m.parameterTypes
                if (parameterTypes != null && parameterTypes.size == 1 && parameterTypes[0].isAssignableFrom(value.javaClass)) {
                    return m
                }
            }
        }
        log.warn("factoryMethod: class " + clazz.name + " does not have static factory method that takes a String, returning null")
        return null
    }

    fun <T> callFactoryMethod(clazz: Class<T>, value: Any): T? {
        val m = factoryMethod(clazz, value)
        return if (m != null) invokeStatic(m, value) as T else null
    }

    fun scrubStrings(thing: Any, fields: Array<String>): Any? {
        if (empty(thing)) return thing
        if (thing.javaClass.isPrimitive
                || thing is String
                || thing is Number
                || thing is Enum<*>)
            return thing

        if (thing is JsonNode) {
            if (thing is ObjectNode) {
                for (field in fields) {
                    if (thing.has(field)) {
                        thing.remove(field)
                    }
                }
            } else if (thing is ArrayNode) {
                for (i in 0 until thing.size()) {
                    scrubStrings(thing.get(i), fields)
                }
            }
        } else if (thing is Map<*, *>) {
            val toRemove = HashSet()
            for (e in thing.entries) {
                val entry = e as Entry<*, *>
                if (ArrayUtils.contains(fields, entry.key.toString())) {
                    toRemove.add(entry.key)
                } else {
                    scrubStrings(entry.value, fields)
                }
            }
            for (key in toRemove) thing.remove(key)

        } else if (Array<Any>::class.java.isAssignableFrom(thing.javaClass)) {
            if (!(thing as Array<Any>)[0].javaClass.isPrimitive) {
                for (obj in thing) {
                    scrubStrings(obj, fields)
                }
            }
        } else if (thing is Collection<*>) {
            for (obj in thing) {
                scrubStrings(obj, fields)
            }
        } else {
            for (field in ReflectionUtil.toMap(thing).keys) {
                val `val` = get<Any>(thing, field, null)
                if (`val` != null) {
                    if (ArrayUtils.contains(fields, field)) {
                        setNull(thing, field, String::class.java)
                    } else {
                        scrubStrings(`val`, fields)
                    }
                }
            }
        }
        return thing
    }

    private enum class Accessor {
        get, set
    }

    /**
     * Copies fields from src to dest. Code is easier to read if this method is understdood to be like an assignment statement, dest = src
     *
     * We consider only 'getter' methods that meet the following criteria:
     * (1) starts with "get"
     * (2) takes zero arguments
     * (3) has a return value
     * (4) does not carry any annotation whose simple class name is "Transient"
     *
     * The value returned from the source getter will be copied to the destination (via setter), if a setter exists, and:
     * (1) No getter exists on the destination, or (2) the destination's getter returns a different value (.equals returns false)
     *
     * Getters that return null values on the source object will not be copied.
     *
     * @param dest destination object
     * @param src source object
     * @param <T> objects must share a type
     * @return count of fields copied
    </T> */
    fun <T> copy(dest: T, src: T): Int {
        return copy(dest, src, null, null)
    }

    /**
     * Same as copy(dest, src) but only named fields are copied
     * @param dest destination object
     * @param src source object
     * @param fields only fields with these names will be considered for copying
     * @param <T> objects must share a type
     * @return count of fields copied
    </T> */
    fun <T> copy(dest: T, src: T, fields: Array<String>?): Int {
        var copyCount = 0
        if (fields != null) {
            for (field in fields) {
                try {
                    val value = get<Any>(src, field, null)
                    if (value != null) {
                        set(dest, field, value)
                        copyCount++
                    }
                } catch (e: Exception) {
                    log.debug("copy: field=$field: $e")
                }

            }
        }
        return copyCount
    }

    /**
     * Same as copy(dest, src) but only named fields are copied
     * @param dest destination object, or a Map<String></String>, Object>
     * @param src source object
     * @param fields only fields with these names will be considered for copying
     * @param exclude fields with these names will NOT be considered for copying
     * @param <T> objects must share a type
     * @return count of fields copied
    </T> */
    fun <T> copy(dest: T, src: T, fields: Array<String>?, exclude: Array<String>?): Int {
        var copyCount = 0
        val isMap = dest is Map<*, *>
        try {
            if (src is Map<*, *>) copyFromMap(dest, src as Map<String, Any>, exclude)

            checkGetter@ for (getter in src.javaClass.getMethods()) {
                // only look for getters on the source object (methods with no arguments that have a return value)
                val types = getter.parameterTypes
                if (types.size != 0) continue
                if (getter.returnType == Void::class.java) continue

                // and it must be named appropriately
                val fieldName = fieldName(getter.name)
                if (fieldName == null || ArrayUtils.contains(exclude, fieldName)) continue

                // if specific fields were given, it must be one of those
                if (fields != null && !ArrayUtils.contains(fields, fieldName)) continue

                // getter must not be marked @Transient
                if (isIgnored(src, fieldName, getter)) continue

                // what would the setter be called?
                val setterName = setterForGetter(getter.name) ?: continue

                // get the setter method on the destination object
                var setter: Method? = null
                if (!isMap) {
                    try {
                        setter = dest.javaClass.getMethod(setterName, getter.returnType)
                    } catch (e: Exception) {
                        log.debug("copy: setter not found: $setterName")
                        continue
                    }

                }

                // do not copy null fields (should this be configurable?)
                val srcValue = getter.invoke(src) ?: continue

                // does the dest have a getter? if so grab the current value
                var destValue: Any? = null
                try {
                    if (isMap) {
                        destValue = (dest as Map<*, *>)[fieldName]
                    } else {
                        destValue = getter.invoke(dest)
                    }
                } catch (e: Exception) {
                    log.debug("copy: error calling getter on dest: $e")
                }

                // copy the value from src to dest, if it's different
                if (srcValue != destValue) {
                    if (isMap) {
                        (dest as Map<*, *>).put(fieldName, srcValue)
                    } else {
                        setter!!.invoke(dest, srcValue)
                    }
                    copyCount++
                }
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Error copying " + dest.javaClass.getSimpleName() + " from src=" + src + ": " + e, e)
        }

        return copyCount
    }

    private fun <T> isIgnored(o: T, fieldName: String, getter: Method): Boolean {
        var field: Field? = null
        try {
            field = o.javaClass.getDeclaredField(fieldName)
        } catch (ignored: NoSuchFieldException) {
        }

        return isIgnored(getter.annotations) || field != null && isIgnored(field.annotations)
    }

    private fun isIgnored(annotations: Array<Annotation>?): Boolean {
        if (annotations != null) {
            for (a in annotations) {
                val interfaces = a.javaClass.interfaces
                if (interfaces != null) {
                    for (i in interfaces) {
                        if (i.simpleName == "Transient") {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    fun fieldName(method: String): String? {
        if (method.startsWith("get")) return uncapitalize(method.substring(3))
        if (method.startsWith("set")) return uncapitalize(method.substring(3))
        return if (method.startsWith("is")) uncapitalize(method.substring(2)) else null
    }

    fun setterForGetter(getter: String): String? {
        if (getter.startsWith("get")) return "set" + getter.substring(3)
        return if (getter.startsWith("is")) "set" + getter.substring(2) else null
    }

    /**
     * Call setters on an object based on keys and values in a Map
     * @param dest destination object
     * @param src map of field name -> value
     * @param <T> type of object
     * @return the destination object
    </T> */
    fun <T> copyFromMap(dest: T?, src: Map<String, Any>): T {
        return copyFromMap(dest, src, null)
    }

    fun <T> copyFromMap(dest: T?, src: Map<String, Any>, exclude: Array<String>?): T {
        for ((key, value) in src) {
            if (exclude != null && ArrayUtils.contains(exclude, key)) continue
            if (value != null && Map<*, *>::class.java.isAssignableFrom(value.javaClass)) {
                if (hasGetter(dest, key)) {
                    val m = value as Map<*, *>
                    if (m.isEmpty()) continue
                    if (m.keys.iterator().next().javaClass == String::class.java) {
                        copyFromMap(get(dest, key), m as Map<String, Any>)
                    } else {
                        log.info("copyFromMap: not recursively copying Map (has non-String keys): $key")
                    }
                }
            } else {
                if (Map<*, *>::class.java.isAssignableFrom(dest!!.javaClass)) {// || dest.getClass().getName().equals(HashMap.class.getName())) {
                    (dest as Map<*, *>).put(key, value)
                } else {
                    if (hasSetter(dest, key, value.javaClass)) {
                        set(dest, key, value)
                    } else {
                        val pc = getPrimitiveClass(value.javaClass)
                        if (pc != null && hasSetter(dest, key, pc)) {
                            set(dest, key, value)
                        } else {
                            log.info("copyFromMap: skipping uncopyable property: $key")
                        }
                    }
                }
            }
        }
        return dest
    }

    fun getPrimitiveClass(clazz: Class<*>): Class<*>? {
        if (clazz.isArray) return arrayClass<Any>(getPrimitiveClass(clazz.componentType)!!)
        when (clazz.simpleName) {
            "Long" -> return Long::class.javaPrimitiveType
            "Integer" -> return Int::class.javaPrimitiveType
            "Short" -> return Short::class.javaPrimitiveType
            "Double" -> return Double::class.javaPrimitiveType
            "Float" -> return Float::class.javaPrimitiveType
            "Boolean" -> return Boolean::class.javaPrimitiveType
            "Character" -> return Char::class.javaPrimitiveType
            else -> return null
        }
    }

    @JvmOverloads
    fun toMap(thing: Any, fields: Array<String>? = null, exclude: Array<String> = TO_MAP_STANDARD_EXCLUDES): Map<String, Any> {
        val map = HashMap<String, Any>()
        copy(map, thing, fields, exclude)
        return map
    }

    /**
     * Find the concrete class for the first declared parameterized class variable
     * @param clazz The class to search for parameterized types
     * @return The first concrete class for a parameterized type found in clazz
     */
    fun <T> getFirstTypeParam(clazz: Class<*>): Class<T> {
        return getTypeParam(clazz, 0)
    }

    fun <T> getTypeParam(clazz: Class<*>, index: Int): Class<T> {
        // todo: add a cache on this thing... could do wonders
        var check: Class<*> = clazz
        while (check.genericSuperclass == null || check.genericSuperclass !is ParameterizedType) {
            check = check.superclass
            if (check == Any::class.java) die<Any>("getTypeParam(" + clazz.name + "): no type parameters found")
        }
        val parameterizedType = check.genericSuperclass as ParameterizedType
        val actualTypeArguments = parameterizedType.actualTypeArguments
        if (index >= actualTypeArguments.size) die<Any>("getTypeParam(" + clazz.name + "): " + actualTypeArguments.size + " type parameters found, index " + index + " out of bounds")
        if (actualTypeArguments[index] is Class<*>) return actualTypeArguments[index] as Class<*>
        return if (actualTypeArguments[index] is ParameterizedType) (actualTypeArguments[index] as ParameterizedType).rawType as Class<*> else (actualTypeArguments[index] as Type).javaClass as Class<T>
    }

    /**
     * Find the concrete class for a parameterized class variable.
     * @param clazz The class to start searching. Search will continue up through superclasses
     * @param impl The type (or a supertype) of the parameterized class variable
     * @return The first concrete class found that is assignable to an instance of impl
     */
    fun <T> getFirstTypeParam(clazz: Class<*>, impl: Class<*>): Class<T>? {
        // todo: add a cache on this thing... could do wonders
        var check: Class<*>? = clazz
        while (check != null && check != Any::class.java) {
            val superCheck = check
            var superType: Type? = superCheck.genericSuperclass
            while (superType != null && superType != Any::class.java) {
                if (superType is ParameterizedType) {
                    val ptype = superType as ParameterizedType?
                    val rawType = ptype!!.rawType as Class<*>
                    if (impl.isAssignableFrom(rawType)) {
                        return rawType as Class<T>
                    }
                    for (t in ptype.actualTypeArguments) {
                        if (impl.isAssignableFrom(t as Class<*>)) {
                            return t as Class<T>
                        }
                    }

                } else if (superType is Class<*>) {
                    superType = superType.genericSuperclass
                }
            }
            check = check.superclass
        }
        return null
    }

    /**
     * Call a getter. getXXX and isXXX will both be checked.
     * @param object the object to call get(field) on
     * @param field the field name
     * @return the value of the field
     * @throws IllegalArgumentException If no getter for the field exists
     */
    operator fun get(`object`: Any, field: String): Any? {
        var target: Any? = `object`
        for (token in field.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            if (target == null) return null
            target = invoke_get(target, token)
        }
        return target
    }

    operator fun <T> get(`object`: Any, field: String, defaultValue: T?): T? {
        try {
            val `val` = get(`object`, field)
            return if (`val` == null) defaultValue else `val` as T?
        } catch (e: Exception) {
            log.warn("get: $e")
            return defaultValue
        }

    }

    fun hasGetter(`object`: Any, field: String): Boolean {
        var target = `object`
        try {
            for (token in field.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                val methodName = getAccessorMethodName(Accessor.get, token)
                target = MethodUtils.invokeExactMethod(target, methodName, null)
            }
        } catch (e: NoSuchMethodException) {
            return false
        } catch (e: Exception) {
            return false
        }

        return true
    }

    fun getterType(`object`: Any, field: String): Class<*> {
        try {
            val o = get(`object`, field) ?: return die("getterType: cannot determine field type, value was null")
            return o.javaClass

        } catch (e: Exception) {
            return die("getterType: simple get failed: $e", e)
        }

    }

    /**
     * Call a setter with a hint as to what the type should be
     * @param object the object to call set(field) on
     * @param field the field name
     * @param value the value to set
     */
    @JvmOverloads
    fun set(`object`: Any, field: String, value: Any?, type: Class<*>? = null) {
        var value = value
        if (type != null) {
            if (value == null) {
                setNull(`object`, field, type)
                return
            }
            value = instantiate<*>(type, value)
        }
        val tokens = field.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val target = getTarget(`object`, tokens)
        if (target != null) invoke_set(target, tokens[tokens.size - 1], value)
    }

    fun setNull(`object`: Any, field: String, type: Class<*>) {
        val tokens = field.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val target = getTarget(`object`, tokens)
        if (target != null) invoke_set_null(target, tokens[tokens.size - 1], type)
    }

    private fun getTarget(`object`: Any, tokens: Array<String>): Any? {
        var target: Any? = `object`
        for (i in 0 until tokens.size - 1) {
            target = invoke_get(target, tokens[i])
            if (target == null) {
                log.warn("getTarget(" + `object` + ", " + Arrays.toString(tokens) + "): exiting early, null object found at token=" + tokens[i])
                return null
            }
        }
        return target
    }

    fun hasSetter(`object`: Any, field: String, type: Class<*>): Boolean {
        var target = `object`
        val tokens = field.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        try {
            for (i in 0 until tokens.size - 1) {
                target = MethodUtils.invokeExactMethod(target, tokens[i], null)
            }

            target.javaClass.getMethod(getAccessorMethodName(Accessor.set, tokens[tokens.size - 1]), type)

        } catch (e: NoSuchMethodException) {
            return false
        } catch (e: Exception) {
            return false
        }

        return true
    }

    private fun getAccessorMethodName(accessor: Accessor, token: String): String {
        return if (token.length == 1) accessor.name + token.toUpperCase() else accessor.name + token.substring(0, 1).toUpperCase() + token.substring(1)
    }

    private fun invoke_get(target: Any, token: String): Any {
        var target = target
        val methodName = getAccessorMethodName(Accessor.get, token)
        try {
            target = MethodUtils.invokeMethod(target, methodName, null)
        } catch (e: Exception) {
            val isMethod = methodName.replaceFirst("get".toRegex(), "is")
            try {
                target = MethodUtils.invokeMethod(target, isMethod, null)
            } catch (e2: Exception) {
                if (target is Map<*, *>) return target.get(token)
                if (target is ObjectNode) return target.get(token)
                throw IllegalArgumentException("Error calling $methodName and $isMethod: $e, $e2")
            }

        }

        return target
    }

    private fun invoke_set(target: Any, token: String, value: Any?) {
        val methodName = getAccessorMethodName(Accessor.set, token)
        if (value == null) {
            // try to find a single-arg method named methodName...
            var found: Method? = null
            for (m in target.javaClass.methods) {
                if (m.name == methodName && m.parameterTypes.size == 1) {
                    if (found != null) {
                        die<Any>("invoke_set: value was null and multiple single-arg methods named $methodName exist")
                    } else {
                        found = m
                    }
                }
            }
            if (found == null) die<Any>("invoke_set: no method $methodName found on target: $target")
            try {
                found!!.invoke(target, *arrayOf<Any>(getNullArgument(found.parameterTypes[0])))
            } catch (e: Exception) {
                die<Any>("Error calling $methodName on target: $target - $e")
            }

        } else {
            try {
                MethodUtils.invokeMethod(target, methodName, value)
            } catch (e: Exception) {
                die<Any>("Error calling $methodName: $e")
            }

        }
    }

    private fun invoke_set_null(target: Any, token: String, type: Class<*>) {
        val methodName = getAccessorMethodName(Accessor.set, token)
        try {
            MethodUtils.invokeMethod(target, methodName, arrayOf<Any>(getNullArgument(type)), arrayOf(type))
        } catch (e: Exception) {
            die<Any>("Error calling $methodName: $e")
        }

    }

    private fun getNullArgument(clazz: Class<*>): Any? {
        if (clazz.isPrimitive) {
            when (clazz.name) {
                "boolean" -> return false
                "byte" -> return 0.toByte()
                "short" -> return 0.toShort()
                "char" -> return 0.toChar()
                "int" -> return 0
                "long" -> return 0.toLong()
                "float" -> return 0.toFloat()
                "double" -> return 0.toDouble()
                else -> return die<Any>("instantiate: unrecognized primitive type: " + clazz.name)
            }
        }
        return null
    }

    // methods below forked from dropwizard-- https://github.com/codahale/dropwizard

    /**
     * Finds the type parameter for the given class.
     *
     * @param klass    a parameterized class
     * @return the class's type parameter
     */
    fun getTypeParameter(klass: Class<*>): Class<*> {
        return getTypeParameter(klass, Any::class.java)
    }

    /**
     * Finds the type parameter for the given class which is assignable to the bound class.
     *
     * @param klass    a parameterized class
     * @param bound    the type bound
     * @param <T>      the type bound
     * @return the class's type parameter
    </T> */
    fun <T> getTypeParameter(klass: Class<*>, bound: Class<in T>): Class<T> {
        var t: Type = checkNotNull(klass)
        while (t is Class<*>) {
            t = t.genericSuperclass
        }
        /* This is not guaranteed to work for all cases with convoluted piping
         * of type parameters: but it can at least resolve straight-forward
         * extension with single type parameter (as per [Issue-89]).
         * And when it fails to do that, will indicate with specific exception.
         */
        if (t is ParameterizedType) {
            // should typically have one of type parameters (first one) that matches:
            for (param in t.actualTypeArguments) {
                if (param is Class<*>) {
                    val cls = determineClass(bound, param)
                    if (cls != null) {
                        return cls
                    }
                } else if (param is TypeVariable<*>) {
                    for (paramBound in param.bounds) {
                        if (paramBound is Class<*>) {
                            val cls = determineClass(bound, paramBound)
                            if (cls != null) {
                                return cls
                            }
                        }
                    }
                }
            }
        }
        return die("Cannot figure out type parameterization for " + klass.name)
    }

    private fun <T> determineClass(bound: Class<in T>, candidate: Type): Class<T>? {
        if (candidate is Class<*>) {
            if (bound.isAssignableFrom(candidate)) {
                return candidate as Class<T>
            }
        }

        return null
    }

    @Throws(Exception::class)
    fun close(o: Any?) {
        if (o == null) return
        if (o is Closeable) {
            o.close()

        } else {
            val closeMethod = o.javaClass.getMethod("close", *null as Array<Class<*>>?)
            if (closeMethod == null) die<Any>("no close method found on " + o.javaClass.name)
            closeMethod!!.invoke(o)
        }
    }

    fun closeQuietly(o: Any?) {
        if (o == null) return
        try {
            close(o)
        } catch (e: Exception) {
            log.warn("close: error closing: $e")
        }

    }

    class Setter<T> {
        var field: String
            protected set
        var value: String
            protected set

        @java.beans.ConstructorProperties("field", "value")
        constructor(field: String, value: String) {
            this.field = field
            this.value = value
        }

        constructor() {}

        fun set(data: T) {
            ReflectionUtil.set(data, field, value)
        }

        override fun toString(): String {
            return javaClass.name + '{'.toString() + field + ", " + value + '}'.toString()
        }
    }

    // adapted from https://stackoverflow.com/a/2924426/1251543
    private class CallerInspector : SecurityManager() {
        val callerClassName: String
            get() = classContext[2].name

        fun getCallerClassName(depth: Int): String {
            return classContext[depth].name
        }
    }

    fun callerClassName(): String {
        return callerInspector.callerClassName
    }

    fun callerClassName(depth: Int): String {
        return callerInspector.getCallerClassName(depth)
    }

    fun callerClassName(match: String): String {
        val s = callerFrame(match)
        return if (s == null) "callerClassName: no match: $match" else s.methodName
    }

    fun callerMethodName(): String {
        return Throwable().stackTrace[2].methodName
    }

    fun callerMethodName(depth: Int): String {
        return Throwable().stackTrace[depth].methodName
    }

    fun callerMethodName(match: String): String {
        val s = callerFrame(match)
        return if (s == null) "callerMethodName: no match: $match" else s.methodName
    }

    fun caller(): String {
        val t = Throwable().stackTrace
        return if (t == null || t.size == 0) "caller: NO STACK TRACE!" else caller(t[Math.max(t.size - 1, 2)])
    }

    fun caller(depth: Int): String {
        val t = Throwable().stackTrace
        return if (t == null || t.size == 0) "caller: NO STACK TRACE!" else caller(t[Math.min(depth, t.size - 1)])
    }

    fun caller(match: String): String {
        val s = callerFrame(match)
        return s?.let { caller(it) } ?: "caller: no match: $match"
    }

    fun callerFrame(match: String): StackTraceElement? {
        val t = Throwable().stackTrace
        if (t == null || t.size == 0) return null
        for (s in t) if (caller(s).contains(match)) return s
        return null
    }

    fun caller(s: StackTraceElement): String {
        return s.className + "." + s.methodName + ":" + s.lineNumber
    }

    /**
     * Replace any string values with their transformed values
     * @param map a map of things
     * @param transformer a transformer
     * @return the same map, but if any value was a string, the transformer has been applied to it.
     */
    fun transformStrings(map: MutableMap<*, *>, transformer: Transformer): Map<*, *>? {
        if (empty(map)) return map
        val setOps = HashMap()
        for (entry in map.entries) {
            val e = entry as Entry<*, *>
            if (e.value is String) {
                setOps.put(e.key, transformer.transform(e.value).toString())
            } else if (e.value is Map<*, *>) {
                setOps.put(e.key, transformStrings(e.value as Map<*, *>, transformer))
            }
        }
        for (entry in setOps.entries) {
            val e = entry as Entry<*, *>
            map[e.key] = e.value
        }
        return map
    }

}
/**
 * Make a copy of the object, assuming its class has a copy constructor
 * @param thing The thing to copy
 * @return A copy of the object, created using the thing's copy constructor
 */
/**
 * Call a setter
 * @param object the object to call set(field) on
 * @param field the field name
 * @param value the value to set
 */

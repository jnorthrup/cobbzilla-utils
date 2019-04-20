package org.cobbzilla.util.collection

import org.cobbzilla.util.string.StringUtil

import java.lang.reflect.Array
import java.util.*

import org.cobbzilla.util.daemon.ZillaRuntime.die
import org.cobbzilla.util.daemon.ZillaRuntime.empty
import org.cobbzilla.util.reflect.ReflectionUtil.arrayClass

object ArrayUtil {

    val SINGLE_NULL_OBJECT = arrayOf<Any>(null)
    val EMPTY_OBJECT_ARRAY = arrayOfNulls<Any>(0)

    fun <T> append(array: Array<T>?, vararg elements: T): Array<T> {
        if (array == null || array!!.size == 0) {
            if (elements.size == 0) return arrayOf<Any>() as Array<T> // punt, it's empty anyway
            val newArray = Array.newInstance(elements[0].javaClass, elements.size) as Array<T>
            System.arraycopy(elements, 0, newArray, 0, elements.size)
            return newArray
        } else {
            if (elements.size == 0) return Arrays.copyOf<T>(array!!, array!!.size)
            val copy = Arrays.copyOf<T>(array!!, array!!.size + elements.size)
            System.arraycopy(elements, 0, copy, array!!.size, elements.size)
            return copy
        }
    }

    fun <T> concat(vararg arrays: Array<T>): Array<T> {
        var size = 0
        for (array in arrays) {
            size += if (array == null) 0 else array!!.size
        }
        val componentType = arrays.javaClass.componentType.componentType
        val newArray = Array.newInstance(componentType, size) as Array<T>
        var destPos = 0
        for (array in arrays) {
            System.arraycopy(array, 0, newArray, destPos, array.size)
            destPos += array.size
        }
        return newArray
    }

    fun <T> remove(array: Array<T>?, indexToRemove: Int): Array<T> {
        if (array == null) throw NullPointerException("remove: array was null")
        if (indexToRemove >= array!!.size || indexToRemove < 0) throw IndexOutOfBoundsException("remove: cannot remove element " + indexToRemove + " from array of length " + array!!.size)
        val list = ArrayList(Arrays.asList<T>(*array!!))
        list.removeAt(indexToRemove)
        val newArray = Array.newInstance(array!!.javaClass.getComponentType(), array!!.size - 1) as Array<T>
        return list.toTypedArray()
    }

    /**
     * Return a slice of an array. If from == to then an empty array will be returned.
     * @param array the source array
     * @param from the start index, inclusive. If less than zero or greater than the length of the array, an Exception is thrown
     * @param to the end index, NOT inclusive. If less than zero or greater than the length of the array, an Exception is thrown
     * @param <T> the of the array
     * @return A slice of the array. The source array is not modified.
    </T> */
    fun <T> slice(array: Array<T>?, from: Int, to: Int): Array<T> {

        if (array == null) throw NullPointerException("slice: array was null")
        if (from < 0 || from > array!!.size) die<Any>("slice: invalid 'from' index (" + from + ") for array of size " + array!!.size)
        if (to < 0 || to < from || to > array!!.size) die<Any>("slice: invalid 'to' index (" + to + ") for array of size " + array!!.size)

        val newArray = Array.newInstance(array!!.javaClass.getComponentType(), to - from) as Array<T>
        if (to == from) return newArray
        System.arraycopy(array!!, from, newArray, 0, to - from)
        return newArray
    }

    fun <T> merge(vararg collections: Collection<T>): List<T> {
        if (empty(collections)) return emptyList()
        val result = HashSet<T>()
        for (c in collections) result.addAll(c)
        return ArrayList(result)
    }

    /**
     * Produce a delimited string from an array.
     * @param array the array to consider
     * @param delim the delimiter to put in between each element
     * @param nullValue the value to write if an array entry is null. if this parameter is null, then null array entries will not be included in the output.
     * @param includeBrackets if false, the return value will not start/end with []
     * @return a string that starts with [ and ends with ] and within is the result of calling .toString on each non-null element (and printing nullValue for each null element, unless nulValue == null in which case null elements are omitted), with 'delim' in between each entry.
     */
    @JvmOverloads
    fun arrayToString(array: Array<Any>?, delim: String, nullValue: String? = "null", includeBrackets: Boolean = true): String {
        if (array == null) return "null"
        val b = StringBuilder(if (includeBrackets) "[" else "")
        for (o in array!!) {
            if (b.length > 0) b.append(delim)
            if (o == null) {
                if (nullValue == null) continue
                b.append(nullValue)
            } else if (o!!.javaClass.isArray()) {
                b.append(arrayToString(o as Array<Any>, delim, nullValue))
            } else if (o is Map<*, *>) {
                b.append(StringUtil.toString(o as Map<*, *>))
            } else {
                b.append(o!!.toString())
            }
        }
        return if (includeBrackets) b.append("]").toString() else b.toString()
    }

    fun <T> shift(args: Array<T>?): Array<T>? {
        if (args == null) return null
        if (args!!.size == 0) return args
        val newArgs = Array.newInstance(args!![0].javaClass, args!!.size - 1) as Array<T>
        System.arraycopy(args!!, 1, newArgs, 0, args!!.size - 1)
        return newArgs
    }

    fun <T> singletonArray(thing: T): Array<T> {
        return singletonArray(thing, thing.javaClass as Class<T>)
    }

    fun <T> singletonArray(thing: T, clazz: Class<T>): Array<T> {
        val array = Array.newInstance(arrayClass<Any>(clazz), 1) as Array<T>
        array[0] = thing
        return array
    }
}
/**
 * Produce a delimited string from an array. Null values will appear as "null"
 * @param array the array to consider
 * @param delim the delimiter to put in between each element
 * @return the result of calling .toString on each array element, or "null" for null elements, separated by the given delimiter.
 */
/**
 * Produce a delimited string from an array.
 * @param array the array to consider
 * @param delim the delimiter to put in between each element
 * @param nullValue the value to write if an array entry is null. if this parameter is null, then null array entries will not be included in the output.
 * @return a string that starts with [ and ends with ] and within is the result of calling .toString on each non-null element (and printing nullValue for each null element, unless nulValue == null in which case null elements are omitted), with 'delim' in between each entry.
 */

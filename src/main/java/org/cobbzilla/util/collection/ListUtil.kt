package org.cobbzilla.util.collection

import com.google.common.collect.Lists
import org.cobbzilla.util.reflect.ReflectionUtil

import java.util.*

object ListUtil {

    fun <T> concat(list1: List<T>?, list2: List<T>?): List<T>? {
        if (list1 == null || list1.isEmpty()) return if (list2 == null) null else ArrayList(list2)
        if (list2 == null || list2.isEmpty()) return ArrayList(list1)
        val newList = ArrayList<T>(list1.size + list2.size)
        newList.addAll(list1)
        newList.addAll(list2)
        return newList
    }

    // adapted from: https://stackoverflow.com/a/23870892/1251543

    /**
     * Combines several collections of elements and create permutations of all of them, taking one element from each
     * collection, and keeping the same order in resultant lists as the one in original list of collections.
     *
     *
     * Example
     *  * Input  = { {a,b,c} , {1,2,3,4} }
     *  * Output = { {a,1} , {a,2} , {a,3} , {a,4} , {b,1} , {b,2} , {b,3} , {b,4} , {c,1} , {c,2} , {c,3} , {c,4} }
     *
     *
     * @param collections Original list of collections which elements have to be combined.
     * @return Resultant collection of lists with all permutations of original list.
     */
    fun <T> permutations(collections: List<List<T>>?): List<List<T>> {
        if (collections == null || collections.isEmpty()) {
            return emptyList()
        } else {
            val res = Lists.newLinkedList<List<T>>()
            permutationsImpl(collections, res, 0, LinkedList())
            return res
        }
    }

    private fun <T> permutationsImpl(ori: List<List<T>>, res: MutableCollection<List<T>>, d: Int, current: List<T>) {
        // if depth equals number of original collections, final reached, add and return
        if (d == ori.size) {
            res.add(current)
            return
        }

        // iterate from current collection and copy 'current' element N times, one for each element
        val currentCollection = ori[d]
        for (element in currentCollection) {
            val copy = Lists.newLinkedList(current)
            copy.add(element)
            permutationsImpl(ori, res, d + 1, copy)
        }
    }

    fun expand(things: Array<Any>, context: Map<String, Any>): List<Any> {
        val results = ArrayList<Any>()
        for (thing in things) {
            if (thing is Expandable<*>) {
                results.addAll(thing.expand(context))
            } else {
                results.add(thing)
            }
        }
        return results
    }

    fun <T> deepCopy(list: List<T>?): List<T>? {
        if (list == null) return null
        val copy = ArrayList<T>()
        for (item in list) copy.add(if (item == null) null else ReflectionUtil.copy(item))
        return copy
    }

}
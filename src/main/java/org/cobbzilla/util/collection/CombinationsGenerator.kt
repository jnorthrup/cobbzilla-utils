package org.cobbzilla.util.collection

import java.util.HashSet
import java.util.LinkedHashSet

object CombinationsGenerator {

    // The main function that gets all combinations of size n-1 to 1, in set of size n.
    // This function mainly uses combinationUtil()
    fun generateCombinations(elements: Set<String>): Set<Set<String>> {
        var result: MutableSet<Set<String>> = LinkedHashSet()

        // i - A number of elements which will be used in the combination in this iteration
        for (i in elements.size - 1 downTo 1) {
            // A temporary array to store all combinations one by one
            val data = arrayOfNulls<String>(i)
            // Get all combination using temporary array 'data'
            result = _generate(result, elements.toTypedArray(),
                    data, 0, elements.size - 1, 0, i)
        }
        return result
    }

    /**
     * @param combinations - Resulting array with all combinations of arr
     * @param arr - Input Array
     * @param data - Temporary array to store current combination
     * @param start - Staring index in arr[]
     * @param end - Ending index in arr[]
     * @param index - Current index in data[]
     * @param r - Size of a combination
     */
    private fun _generate(combinations: MutableSet<Set<String>>, arr: Array<String>,
                          data: Array<String>, start: Int, end: Int, index: Int, r: Int): MutableSet<Set<String>> {
        var combinations = combinations
        // Current combination is ready
        if (index == r) {
            val current = HashSet<String>()
            for (j in 0 until r) {
                current.add(data[j])
            }
            combinations.add(current)
            return combinations
        }

        // replace index with all possible elements. The condition `end - i + 1 >= r - index` makes sure that including
        // one element at index will make a combination with remaining elements at remaining positions
        var i = start
        while (i <= end && end - i + 1 >= r - index) {
            data[index] = arr[i]
            combinations = _generate(combinations, arr, data, i + 1, end, index + 1, r)
            i++
        }

        return combinations
    }
}

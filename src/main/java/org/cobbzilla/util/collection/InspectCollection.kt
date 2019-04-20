package org.cobbzilla.util.collection

import java.util.*

object InspectCollection {

    fun containsCircularReference(start: String, graph: Map<String, List<String>>): Boolean {
        return containsCircularReference(HashSet(), start, graph)
    }

    fun containsCircularReference(found: MutableSet<String>, start: String, graph: Map<String, List<String>>): Boolean {
        val descendents = graph[start] ?: return false
// special case: our starting point is outside the graph.
        for (target in descendents) {
            if (found.contains(target)) {
                // we've seen this target already, we have a circular reference
                return true
            }
            if (graph.containsKey(target)) {
                // this target is also a member of the graph -- add to found and recurse
                found.add(target)
                if (containsCircularReference(HashSet(found), target, graph)) return true
            }
            // no "else" clause here: we don't care about anything not in the graph, it can't create a circular reference.
        }
        return false
    }

    fun isLargerThan(c: Collection<*>, size: Int): Boolean {
        var count = 0
        val i = c.iterator()
        while (i.hasNext() && count <= size) {
            i.next()
            count++
        }
        return count > size
    }

}

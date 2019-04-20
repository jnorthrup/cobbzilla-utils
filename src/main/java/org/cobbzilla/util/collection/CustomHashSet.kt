package org.cobbzilla.util.collection

import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import lombok.experimental.Accessors

import java.util.*
import java.util.concurrent.ConcurrentHashMap

@NoArgsConstructor
@Accessors(chain = true)
class CustomHashSet<E> : Set<E> {

    @Getter
    @Setter
    val elementClass: Class<E>
    @Getter
    @Setter
    val hasher: Hasher<*>

    private val map = ConcurrentHashMap<String, E>()

    interface Hasher<E> {
        fun hash(thing: E): String
    }

    constructor(clazz: Class<E>, hasher: Hasher<E>, collection: Collection<E>) : this(clazz, hasher) {
        addAll(collection)
    }

    constructor(elementClass: Class<E>, hasher: Hasher<E>) {
        this.elementClass = elementClass
        this.hasher = hasher
    }

    override fun size(): Int {
        return map.size
    }

    override fun isEmpty(): Boolean {
        return map.isEmpty()
    }

    override operator fun contains(o: Any?): Boolean {
        if (o == null) return false
        if (elementClass.isAssignableFrom(o.javaClass)) {
            return map.containsKey(hasher.hash(o))

        } else if (o is String) {
            return map.containsKey(o)
        }
        return false
    }

    override fun iterator(): Iterator<E> {
        return map.values.iterator()
    }

    override fun toArray(): Array<Any> {
        return map.values.toTypedArray()
    }

    override fun <T> toArray(a: Array<T>): Array<T> {
        return map.values.toTypedArray() as Array<T>
    }

    override fun add(e: E): Boolean {
        return map.put(hasher.hash(e), e) == null
    }

    fun find(e: E): E {
        return map[hasher.hash(e)]
    }

    override fun remove(o: Any): Boolean {
        if (elementClass.isAssignableFrom(o.javaClass)) {
            return map.remove(hasher.hash(o)) != null

        } else if (o is String) {
            return map.remove(o) != null
        }
        return false
    }

    override fun containsAll(c: Collection<*>): Boolean {
        for (o in c) if (!contains(o)) return false
        return true
    }

    override fun addAll(c: Collection<E>): Boolean {
        var anyAdded = false
        for (o in c) if (!add(o)) anyAdded = true
        return anyAdded
    }

    override fun retainAll(c: Collection<*>): Boolean {
        val toRemove = HashSet<String>()
        for ((key, value) in map) {
            if (!c.contains(value)) toRemove.add(key)
        }
        for (k in toRemove) remove(k)
        return !toRemove.isEmpty()
    }

    override fun removeAll(c: Collection<*>): Boolean {
        var anyRemoved = false
        for (o in c) if (map.remove(o) != null) anyRemoved = true
        return anyRemoved
    }

    override fun clear() {
        map.clear()
    }

}

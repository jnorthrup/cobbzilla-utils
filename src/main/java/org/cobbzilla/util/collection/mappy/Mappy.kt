package org.cobbzilla.util.collection.mappy

import lombok.Getter
import lombok.experimental.Accessors
import org.cobbzilla.util.string.StringUtil

import java.util.*
import java.util.concurrent.ConcurrentHashMap

import org.cobbzilla.util.reflect.ReflectionUtil.getTypeParam

/**
 * Mappy is a map of keys to collections of values. The collection type is configurable and there are several
 * subclasses available. See MappyList, MappySet, MappySortedSet, and MappyConcurrentSortedSet
 *
 * It can be viewed either as a mapping of K->V or as K->C->V
 *
 * Mappy objects are meant to be short-lived. While methods are generally thread-safe, the getter will create a new empty
 * collection every time a key is not found. So it makes a horrible cache. Mappy instances are best suited to be value
 * objects of limited scope.
 *
 * @param <K> key class
 * @param <V> value class
 * @param <C> collection class
</C></V></K> */
@Accessors(chain = true)
abstract class Mappy<K, V, C : Collection<V>> : Map<K, V> {

    private val map: ConcurrentHashMap<K, C>

    @Getter(lazy = true)
    val valueClass = initValueClass()

    private fun initValueClass(): Class<C> {
        return getTypeParam(javaClass, 2)
    }

    constructor() {
        map = ConcurrentHashMap()
    }

    constructor(size: Int) {
        map = ConcurrentHashMap(size)
    }

    constructor(other: Map<K, Collection<V>>) : this() {
        for ((key, value) in other) {
            putAll(key, value)
        }
    }


    /**
     * For subclasses to override and provide their own collection types
     * @return A new (empty) instance of the collection type
     */
    protected abstract fun newCollection(): C

    /**
     * @return the number of key mappings
     */
    override fun size(): Int {
        return map.size
    }

    /**
     * @return the total number of values (may be higher than # of keys)
     */
    fun totalSize(): Int {
        var count = 0
        for (c in allValues()) count += c.size
        return count
    }

    /**
     * @return true if this Mappy contains no values. It may contain keys whose collections have no values.
     */
    override fun isEmpty(): Boolean {
        return flatten().isEmpty()
    }

    override fun containsKey(key: Any): Boolean {
        return map.containsKey(key)
    }

    /**
     * @param value the value to check
     * @return true if the Mappy contains any collection that contains the value, which should be of type V
     */
    override fun containsValue(value: Any): Boolean {
        for (collection in allValues()) {

            if (collection.contains(value)) return true
        }
        return false
    }

    /**
     * @param key the key to find
     * @return the first value in the collection for they key, or null if the collection is empty
     */
    override operator fun get(key: Any): V? {
        val collection = getAll(key as K)
        return if (collection.isEmpty()) null else firstInCollection(collection)
    }

    protected open fun firstInCollection(collection: C): V {
        return collection.iterator().next()
    }

    /**
     * Get the collection of values for a key. This method never returns null.
     * @param key the key to find
     * @return the collection of values for the key, which may be empty
     */
    fun getAll(key: K): C {
        var collection: C? = map[key]
        if (collection == null) {
            collection = newCollection()
            map[key] = collection
        }
        return collection
    }

    /**
     * Add a mapping.
     * @param key the key to add
     * @param value the value to add
     * @return the value passed in, if the map already contained the item. null otherwise.
     */
    override fun put(key: K, value: V): V? {
        var rval: V? = null
        synchronized(map) {
            var group: C? = map[key]
            if (group == null) {
                group = newCollection()
                map[key] = group
            } else {
                rval = if (group.contains(value)) value else null
            }
            group.add(value)
        }
        return rval
    }

    /**
     * Remove a key
     * @param key the key to remove
     * @return The first value in the collection that was referenced by the key
     */
    override fun remove(key: Any): V? {
        val group = map.remove(key)
        return if (group == null || group.isEmpty()) null else group.iterator().next() // empty case should never happen, but just in case
    }

    /**
     * Put a bunch of stuff into the map
     * @param m mappings to add
     */
    override fun putAll(m: Map<out K, V>) {
        for ((key, value) in m) {
            put(key, value)
        }
    }

    /**
     * Put a bunch of stuff into the map
     * @param key the key to add
     * @param values the values to add to the key's collection
     */
    fun putAll(key: K, values: Collection<V>) {
        synchronized(map) {
            var collection: C? = getAll(key)
            if (collection == null) collection = newCollection()
            collection.addAll(values)
            map.put(key, collection)
        }
    }

    /**
     * Erase the entire map.
     */
    override fun clear() {
        map.clear()
    }

    override fun keySet(): Set<K> {
        return map.keys
    }

    override fun values(): Collection<V> {
        val vals = ArrayList<V>()
        for (collection in map.values) vals.addAll(collection)
        return vals
    }

    override fun entrySet(): Set<Entry<K, V>> {
        val entries = HashSet<Entry<K, V>>()
        for ((key, value) in map) {
            for (item in value) {
                entries.add(AbstractMap.SimpleEntry(key, item))
            }
        }
        return entries
    }

    fun allValues(): Collection<C> {
        return map.values
    }

    fun allEntrySets(): Set<Entry<K, C>> {
        return map.entries
    }

    fun flatten(): List<V> {
        val values = ArrayList<V>()
        for (collection in allValues()) values.addAll(collection)
        return values
    }

    fun flatten(values: MutableCollection<V>): List<V> {
        for (collection in allValues()) values.addAll(collection)
        return ArrayList(values)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val other = o as Mappy<*, *, *>?

        if (totalSize() != other!!.totalSize()) return false

        for (key in keys) {
            if (!other.containsKey(key)) return false
            val otherValues = other.getAll(key)
            val thisValues = getAll(key)
            if (otherValues.size != thisValues.size) return false
            for (value in thisValues) {
                if (!otherValues.contains(value)) return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        var result = totalSize().hashCode()
        result = 31 * result + (valueClass?.hashCode() ?: 0)
        for (key in keys) {
            result = 31 * result + (key.hashCode() + 13)
            for (value in getAll(key)) {
                result = 31 * result + (value?.hashCode() ?: 0)
            }
        }
        return result
    }

    override fun toString(): String {
        val b = StringBuilder()
        for (key in keys) {
            if (b.length > 0) b.append(" | ")
            b.append(key).append("->(").append(StringUtil.toString(getAll(key), ", ")).append(")")
        }
        return "{$b}"
    }

    fun toMap(): Map<K, C> {
        val m = HashMap<K, C>()
        for (key in keys) m[key] = getAll(key)
        return m
    }
}

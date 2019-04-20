package org.cobbzilla.util.collection

import lombok.AllArgsConstructor
import lombok.Getter
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.collections.Transformer
import org.cobbzilla.util.reflect.ReflectionUtil

import java.lang.reflect.Array
import java.util.HashSet

import org.cobbzilla.util.collection.ArrayUtil.EMPTY_OBJECT_ARRAY

@AllArgsConstructor
class FieldTransformer : Transformer {

    @Getter
    val field: String? = null

    override fun transform(o: Any): Any? {
        return ReflectionUtil.get(o, field!!)
    }

    fun <E> collect(c: Collection<*>?): List<E>? {
        return if (c == null) null else CollectionUtils.collect(c, this) as List<E>
    }

    fun <E> collectSet(c: Collection<*>?): Set<E>? {
        return if (c == null) null else HashSet(CollectionUtils.collect(c, this))
    }

    fun <E> array(c: Collection<*>?): Array<E>? {
        if (c == null) return null
        if (c.isEmpty()) return EMPTY_OBJECT_ARRAY as Array<E>
        val collect = CollectionUtils.collect(c, this) as List<E>
        val elementType = ReflectionUtil.getterType(c.iterator().next(), field) as Class<E>
        return collect.toTypedArray()
    }

    companion object {

        val TO_NAME = FieldTransformer("name")
        val TO_ID = FieldTransformer("id")
        val TO_UUID = FieldTransformer("uuid")
    }

}
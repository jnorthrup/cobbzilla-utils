package org.cobbzilla.util.collection

import org.apache.commons.collections.Transformer

class ToStringTransformer : Transformer {

    override fun transform(o: Any?): Any {
        return o?.toString() ?: "null"
    }

    companion object {

        val instance = ToStringTransformer()
    }

}

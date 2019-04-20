package org.cobbzilla.util.collection

import lombok.AllArgsConstructor
import lombok.Getter
import org.apache.commons.collections.Transformer

@AllArgsConstructor
class StringPrefixTransformer : Transformer {

    @Getter
    val prefix: String? = null

    override fun transform(input: Any): Any {
        return prefix!! + input.toString()
    }

}

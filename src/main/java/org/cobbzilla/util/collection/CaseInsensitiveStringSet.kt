package org.cobbzilla.util.collection

import java.util.TreeSet

class CaseInsensitiveStringSet : TreeSet<String> {

    constructor() : super(String.CASE_INSENSITIVE_ORDER) {}

    constructor(c: Collection<String>) {
        addAll(c)
    }

}

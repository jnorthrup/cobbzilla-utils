package org.cobbzilla.util.json

import com.fasterxml.jackson.annotation.JsonCreator

enum class JsonEditOperationType {

    read, write, delete, sort;


    companion object {

        @JsonCreator
        fun create(value: String): JsonEditOperationType {
            return valueOf(value.toLowerCase())
        }
    }

}

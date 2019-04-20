package org.cobbzilla.util.security

import com.fasterxml.jackson.annotation.JsonCreator

enum class HashType {

    sha256;


    companion object {

        @JsonCreator
        fun create(value: String): HashType {
            return valueOf(value.toLowerCase())
        }
    }
}

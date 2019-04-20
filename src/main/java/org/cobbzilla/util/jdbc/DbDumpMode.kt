package org.cobbzilla.util.jdbc

import com.fasterxml.jackson.annotation.JsonCreator

enum class DbDumpMode {

    all, schema, data;


    companion object {

        @JsonCreator
        fun fromString(`val`: String): DbDumpMode {
            return valueOf(`val`.toLowerCase())
        }
    }

}

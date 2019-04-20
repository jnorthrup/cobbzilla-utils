package org.cobbzilla.util.time

import com.fasterxml.jackson.annotation.JsonCreator

enum class TimeRelativeType {

    past, present, future;


    companion object {

        @JsonCreator
        fun fromString(`val`: String): TimeRelativeType {
            return valueOf(`val`.toLowerCase())
        }
    }

}

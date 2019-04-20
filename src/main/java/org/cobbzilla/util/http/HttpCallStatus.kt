package org.cobbzilla.util.http

import com.fasterxml.jackson.annotation.JsonCreator

enum class HttpCallStatus {

    initialized, pending, requested, received_response, success, error, timeout;


    companion object {

        @JsonCreator
        fun fromString(`val`: String): HttpCallStatus {
            return valueOf(`val`.toLowerCase())
        }
    }

}

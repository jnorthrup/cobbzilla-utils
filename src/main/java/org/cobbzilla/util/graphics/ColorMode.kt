package org.cobbzilla.util.graphics

import com.fasterxml.jackson.annotation.JsonCreator

enum class ColorMode {

    rgb, ansi;


    companion object {

        @JsonCreator
        fun fromString(`val`: String): ColorMode {
            return valueOf(`val`.toLowerCase())
        }
    }

}

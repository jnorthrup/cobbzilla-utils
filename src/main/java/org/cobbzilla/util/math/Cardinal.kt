package org.cobbzilla.util.math

import com.fasterxml.jackson.annotation.JsonCreator

enum class Cardinal private constructor(val direction: Int, vararg allAliases: String) {

    north(1, "N", "north"),
    east(1, "E", "east"),
    south(-1, "S", "south"),
    west(-1, "W", "west");

    val allAliases: Array<String>

    init {
        this.allAliases = allAliases
    }

    override fun toString(): String {
        return allAliases[0]
    }

    companion object {

        @JsonCreator
        fun create(`val`: String): Cardinal? {
            for (c in values()) {
                for (a in c.allAliases) {
                    if (a.equals(`val`, ignoreCase = true)) return c
                }
            }
            return null
        }

        fun isCardinal(`val`: String): Boolean {
            try {
                return create(`val`.toLowerCase()) != null
            } catch (e: Exception) {
                return false
            }

        }
    }
}

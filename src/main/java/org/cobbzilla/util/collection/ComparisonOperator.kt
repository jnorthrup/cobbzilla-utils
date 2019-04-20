package org.cobbzilla.util.collection

import com.fasterxml.jackson.annotation.JsonCreator
import lombok.AllArgsConstructor
import lombok.Getter

@AllArgsConstructor
enum class ComparisonOperator {

    lt("<", "<", "-lt"),
    le("<=", "<=", "-le"),
    eq("=", "==", "-eq"),
    ge(">=", ">=", "-ge"),
    gt(">", ">", "-gt"),
    ne("!=", "!=", "-ne");

    @Getter
    val sql: String? = null
    @Getter
    val java: String? = null
    @Getter
    val shell: String? = null

    companion object {

        @JsonCreator
        fun fromString(`val`: String): ComparisonOperator {
            return valueOf(`val`.toLowerCase())
        }
    }

}

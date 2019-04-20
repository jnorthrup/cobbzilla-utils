package org.cobbzilla.util.system

import java.util.LinkedHashMap

class MultiCommandResult {

    private val results = LinkedHashMap<Command, CommandResult>()

    fun hasException(): Boolean {
        for (result in results.values) {
            if (result.hasException()) return true
        }
        return false
    }

    fun add(command: Command, commandResult: CommandResult) {
        results[command] = commandResult
    }

    override fun toString(): String {
        return "MultiCommandResult{" +
                "results=" + results +
                ", exception=" + hasException() +
                '}'.toString()
    }

    fun getResults(): Map<Command, CommandResult> {
        return this.results
    }
}

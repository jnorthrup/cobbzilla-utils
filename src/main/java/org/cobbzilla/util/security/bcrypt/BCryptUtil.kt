package org.cobbzilla.util.security.bcrypt

import lombok.extern.slf4j.Slf4j

import java.security.SecureRandom

@Slf4j
object BCryptUtil {

    private val random = SecureRandom()

    @Volatile
    var bcryptRounds: Int? = null
        private set

    @Synchronized
    fun setBcryptRounds(rounds: Int) {
        if (bcryptRounds != null) {
            log.warn("Cannot change bcryptRounds after initialization")
            return
        }
        bcryptRounds = if (rounds < 4) 4 else rounds // 4 is minimum bcrypt rounds
        log.info("setBcryptRounds: initialized with $bcryptRounds rounds (param was $rounds)")
    }

    fun hash(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt(bcryptRounds!!, random))
    }

    @JvmStatic
    fun main(args: Array<String>) {
        var input = 0
        var rounds = 16
        try {
            rounds = Integer.valueOf(args[0])
            input = 1
        } catch (ignored: Exception) {
            // whatever
        }

        setBcryptRounds(rounds)
        println(hash(args[input]))
    }
}

package org.cobbzilla.util.security

import java.io.InputStream
import java.io.OutputStream

interface Crypto {

    val secretKey: String

    fun encrypt(plaintext: String): String

    fun decrypt(ciphertext: String): String

    fun encrypt(`in`: InputStream, out: OutputStream)

    fun decryptBytes(`in`: InputStream): ByteArray

    fun decryptStream(`in`: InputStream): InputStream

}

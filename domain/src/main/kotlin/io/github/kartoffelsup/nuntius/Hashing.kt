package io.github.kartoffelsup.nuntius

import java.security.MessageDigest

fun sha512(payload: ByteArray): Sha512Hashed =
    Sha512Hashed(
        MessageDigest.getInstance("SHA-512").also { it.update(payload) }.digest()
    )

class Sha512Hashed(val payload: ByteArray)

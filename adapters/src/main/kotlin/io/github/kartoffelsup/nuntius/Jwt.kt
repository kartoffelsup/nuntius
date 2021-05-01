package io.github.kartoffelsup.nuntius

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.github.kartoffelsup.nuntius.dtos.AuthenticatedUser
import java.time.ZonedDateTime
import java.util.Date
import java.util.UUID

private const val nuntius = "nuntius"

val algorithm: Algorithm = Algorithm.HMAC256(UUID.randomUUID().toString())
val jwtVerifier: JWTVerifier = makeJwtVerifier()

private fun makeJwtVerifier(): JWTVerifier {
    return JWT
        .require(algorithm)
        .withAudience(nuntius)
        .withIssuer(nuntius)
        .build()
}

fun createJwt(authUser: AuthenticatedUser): Jwt {
    return Jwt(
        JWT.create()
            .withAudience(nuntius)
            .withIssuer(nuntius)
            .withExpiresAt(Date.from(ZonedDateTime.now().plusHours(2L).toInstant()))
            .withClaim("id", authUser.user.uuid.value)
            .sign(algorithm)
    )
}

inline class Jwt(val value: String)

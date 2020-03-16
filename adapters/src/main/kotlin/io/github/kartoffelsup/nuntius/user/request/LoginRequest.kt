package io.github.kartoffelsup.nuntius.user.request

import kotlinx.serialization.Serializable


@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)
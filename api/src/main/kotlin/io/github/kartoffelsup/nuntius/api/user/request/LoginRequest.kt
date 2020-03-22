package io.github.kartoffelsup.nuntius.api.user.request

import kotlinx.serialization.Serializable


@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)
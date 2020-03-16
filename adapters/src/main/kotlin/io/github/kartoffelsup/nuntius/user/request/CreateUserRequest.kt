package io.github.kartoffelsup.nuntius.user.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(val username: String, val email: String, val password: String)
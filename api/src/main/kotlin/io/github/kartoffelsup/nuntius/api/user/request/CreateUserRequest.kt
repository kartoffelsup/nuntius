package io.github.kartoffelsup.nuntius.api.user.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(val username: String, val email: String, val password: String)

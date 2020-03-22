package io.github.kartoffelsup.nuntius.api.user.result

import kotlinx.serialization.Serializable

sealed class LoginResult

@Serializable
data class SuccessfulLogin(val userId: String, val username: String, val token: String) : LoginResult()
@Serializable
data class FailedLogin(val message: String) : LoginResult()

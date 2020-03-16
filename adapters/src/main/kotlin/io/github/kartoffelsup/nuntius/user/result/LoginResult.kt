package io.github.kartoffelsup.nuntius.user.result

import kotlinx.serialization.Serializable

sealed class LoginResult

@Serializable
data class SuccessfulLogin(val token: String) : LoginResult()
@Serializable
data class FailedLogin(val message: String) : LoginResult()
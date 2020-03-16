package io.github.kartoffelsup.nuntius.user.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateNotificationTokenRequest(val token: String)
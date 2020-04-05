package io.github.kartoffelsup.nuntius.api.notification

import kotlinx.serialization.Serializable

@Serializable
sealed class NuntiusNotificationDto

@Serializable
data class MessageNotificationDto(val senderId: String) : NuntiusNotificationDto()

package io.github.kartoffelsup.nuntius.dtos

import kotlinx.serialization.Serializable

@Serializable
sealed class NuntiusNotification

@Serializable
data class MessageNotification(val senderId: String) : NuntiusNotification()

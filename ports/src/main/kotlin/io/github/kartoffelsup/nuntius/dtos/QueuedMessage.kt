package io.github.kartoffelsup.nuntius.dtos

import io.github.kartoffelsup.nuntius.serializers.ZonedDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

@Serializable
data class QueuedMessage(
    val messageId: MessageId,
    val sender: UserId,
    val recipient: UserId,
    @Serializable(with = ZonedDateTimeSerializer::class)
    val timeOfServerArrival: ZonedDateTime,
    val payload: String
)

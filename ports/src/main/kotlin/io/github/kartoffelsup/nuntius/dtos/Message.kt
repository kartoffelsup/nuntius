package io.github.kartoffelsup.nuntius.dtos

import io.github.kartoffelsup.nuntius.serializers.ZonedDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

@Serializable
data class Message(
    val data: String,
    val sender: User,
    val recipient: User,
    @Serializable(with = ZonedDateTimeSerializer::class)
    val sendTimestamp: ZonedDateTime?,
    @Serializable(with = ZonedDateTimeSerializer::class)
    val receiveTimestamp: ZonedDateTime?,
    @Serializable(with = ZonedDateTimeSerializer::class)
    val deliveryTimestamp: ZonedDateTime?,
    val attachments: List<Attachment>
)

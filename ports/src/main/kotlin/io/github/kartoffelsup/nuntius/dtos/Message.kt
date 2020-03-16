package io.github.kartoffelsup.nuntius.dtos

import arrow.core.NonEmptyList
import arrow.core.Option
import io.github.kartoffelsup.nuntius.serializers.NonEmptyListSerializer
import io.github.kartoffelsup.nuntius.serializers.OptionSerializer
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
    @Serializable(with = OptionSerializer::class)
    val attachments: Option<@Serializable(NonEmptyListSerializer::class) NonEmptyList<Attachment>>
)

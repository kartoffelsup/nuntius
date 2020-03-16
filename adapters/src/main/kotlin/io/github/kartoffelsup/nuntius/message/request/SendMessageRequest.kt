package io.github.kartoffelsup.nuntius.message.request

import arrow.core.NonEmptyList
import arrow.core.Option
import io.github.kartoffelsup.nuntius.dtos.Attachment
import io.github.kartoffelsup.nuntius.dtos.UserId
import io.github.kartoffelsup.nuntius.serializers.NonEmptyListSerializer
import io.github.kartoffelsup.nuntius.serializers.OptionSerializer
import io.github.kartoffelsup.nuntius.serializers.ZonedDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

@Serializable
data class SendMessageRequest(
    val text: String,
    val recipient: UserId,
    @Serializable(ZonedDateTimeSerializer::class)
    val sendTimestamp: ZonedDateTime,
    @Serializable(OptionSerializer::class)
    val attachments: Option<@Serializable(NonEmptyListSerializer::class) NonEmptyList<Attachment>> = Option.empty()
)

package io.github.kartoffelsup.nuntius.serializers

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Serializer(forClass = ZonedDateTime::class)
object ZonedDateTimeSerializer :
    KSerializer<ZonedDateTime> {
    private val format = DateTimeFormatter.ISO_ZONED_DATE_TIME

    override fun serialize(encoder: Encoder, value: ZonedDateTime) {
        encoder.encodeString(format.format(value))
    }

    override fun deserialize(decoder: Decoder): ZonedDateTime {
        return ZonedDateTime.parse(decoder.decodeString(),
            format
        )
    }
}
package io.github.kartoffelsup.nuntius.serializers

import arrow.core.Option
import arrow.core.some
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.decode
import kotlinx.serialization.encode

@Serializer(forClass = Option::class)
class OptionSerializer<T : Any?>(val dataSerializer: KSerializer<T>) : KSerializer<Option<T>> {
    override val descriptor: SerialDescriptor
        get() = SerialDescriptor("arrow.core.Option")

    override fun deserialize(decoder: Decoder): Option<T> {
        return if (decoder.decodeNotNullMark()) {
            decoder.decode(dataSerializer).some()
        } else {
            Option.empty<T>()
        }
    }

    override fun serialize(encoder: Encoder, value: Option<T>) {
        value.fold(ifEmpty = { encoder.encodeNull() }, ifSome = { encoder.encode(dataSerializer, it) })
    }
}


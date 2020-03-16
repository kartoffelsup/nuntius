package io.github.kartoffelsup.nuntius.serializers

import arrow.core.NonEmptyList
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.decode
import kotlinx.serialization.encode

@Serializer(forClass = NonEmptyList::class)
class NonEmptyListSerializer<T : Any>(val dataSerializer: KSerializer<T>) :
    KSerializer<NonEmptyList<T>> {
    override val descriptor: SerialDescriptor
        get() = SerialDescriptor("arrow.core.NonEmptyList")

    override fun deserialize(decoder: Decoder): NonEmptyList<T> {
        return NonEmptyList.fromListUnsafe(
            decoder.decode(
                ListSerializer(
                    dataSerializer
                )
            )
        )
    }

    override fun serialize(encoder: Encoder, value: NonEmptyList<T>) {
        encoder.encode(ListSerializer(dataSerializer), value.toList())
    }
}
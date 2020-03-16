package io.github.kartoffelsup.nuntius.dtos

import io.github.kartoffelsup.nuntius.serializers.ZonedDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.time.ZonedDateTime

@Serializable
data class User(
    val uuid: UserId,
    val username: Username,
    val email: Email,
    @Serializable(with = ZonedDateTimeSerializer::class)
    val createdAt: ZonedDateTime,
    @Serializable(with = ZonedDateTimeSerializer::class)
    val lastLogin: ZonedDateTime?
) {
    @Transient
    var password: Password? = null
}

@Serializable
data class UserId(val value: String)
class Password(val value: ByteArray)
@Serializable
data class Email(val value: String)
@Serializable
data class Username(val value: String)

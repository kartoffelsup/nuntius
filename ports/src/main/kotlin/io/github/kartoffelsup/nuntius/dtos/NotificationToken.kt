package io.github.kartoffelsup.nuntius.dtos

import java.time.ZonedDateTime

data class NotificationToken(
    val userId: UserId,
    val token: String,
    val lastUpdate: ZonedDateTime
)
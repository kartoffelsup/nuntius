package io.github.kartoffelsup.nuntius.ports.provided

import arrow.core.NonEmptyList
import arrow.core.Option
import arrow.core.Tuple2
import arrow.core.Tuple3
import io.github.kartoffelsup.nuntius.dtos.Message
import io.github.kartoffelsup.nuntius.dtos.MessageId
import io.github.kartoffelsup.nuntius.dtos.UserId
import java.time.ZonedDateTime

interface MessageQueueService {
    suspend fun enqueue(message: Tuple3<MessageId, ZonedDateTime, Message>): Unit
    suspend fun findQueuedMessages(userId: UserId): Option<NonEmptyList<Tuple2<MessageId, Message>>>
    suspend fun remove(messageId: MessageId): Unit
}
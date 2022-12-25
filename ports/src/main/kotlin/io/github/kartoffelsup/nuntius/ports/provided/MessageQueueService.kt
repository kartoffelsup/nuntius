package io.github.kartoffelsup.nuntius.ports.provided

import arrow.core.NonEmptyList
import io.github.kartoffelsup.nuntius.dtos.Message
import io.github.kartoffelsup.nuntius.dtos.MessageId
import io.github.kartoffelsup.nuntius.dtos.UserId
import java.time.ZonedDateTime

interface MessageQueueService {
    suspend fun enqueue(message: Triple<MessageId, ZonedDateTime, Message>): Unit
    suspend fun findQueuedMessages(userId: UserId): NonEmptyList<Pair<MessageId, Message>>?
    suspend fun remove(messageId: MessageId): Unit
}
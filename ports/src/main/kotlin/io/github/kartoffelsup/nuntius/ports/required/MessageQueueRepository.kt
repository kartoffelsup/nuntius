package io.github.kartoffelsup.nuntius.ports.required

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.Option
import arrow.core.Tuple2
import arrow.core.Tuple3
import io.github.kartoffelsup.nuntius.dtos.Message
import io.github.kartoffelsup.nuntius.dtos.MessageId
import io.github.kartoffelsup.nuntius.dtos.QueuedMessage
import io.github.kartoffelsup.nuntius.dtos.UserId
import java.time.ZonedDateTime

interface MessageQueueRepository {
    suspend fun save(idArrivalMessage: Tuple3<MessageId, ZonedDateTime, Message>): Either<String, QueuedMessage>
    suspend fun findQueuedMessages(userId: UserId): Option<NonEmptyList<Tuple2<MessageId, Message>>>
    suspend fun remove(messageId: MessageId): Unit
}

package io.github.kartoffelsup.nuntius.message

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.left
import arrow.core.right
import arrow.core.toNonEmptyListOrNull
import com.querydsl.sql.SQLQueryFactory
import io.github.kartoffelsup.nuntius.dtos.Message
import io.github.kartoffelsup.nuntius.dtos.MessageId
import io.github.kartoffelsup.nuntius.dtos.QueuedMessage
import io.github.kartoffelsup.nuntius.dtos.UserId
import io.github.kartoffelsup.nuntius.ports.required.MessageQueueRepository
import io.github.kartoffelsup.nuntius.sql.QMessageQueue.messageQueue
import kotlinx.serialization.StringFormat
import java.time.ZonedDateTime

class MessageQueueRepositoryImpl(
    private val stringFormat: StringFormat,
    private val sqlQueryFactory: SQLQueryFactory
) : MessageQueueRepository {

    override suspend fun save(idArrivalMessage: Triple<MessageId, ZonedDateTime, Message>): Either<String, QueuedMessage> {
        val (messageId, arrival, message) = idArrivalMessage
        sqlQueryFactory.insert(messageQueue)
            .set(messageQueue.messageId, messageId.value)
            .set(messageQueue.sender, message.sender.uuid.value)
            .set(messageQueue.recipient, message.recipient.uuid.value)
            .set(messageQueue.timeOfServerArrival, arrival)
            .set(
                messageQueue.payload,
                stringFormat.encodeToString(Message.serializer(), message).toByteArray(Charsets.UTF_8)
            )
            .execute()
        return findOne(messageId)
    }

    override suspend fun findQueuedMessages(userId: UserId): NonEmptyList<Pair<MessageId, Message>>? {
        val messages = sqlQueryFactory.select(messageQueue)
            .from(messageQueue)
            .where(messageQueue.recipient.eq(userId.value))
            .fetch()
            .map {
                MessageId(it.messageId) to stringFormat.decodeFromString(
                    Message.serializer(),
                    it.payload.toString(Charsets.UTF_8)
                )
            }

        return messages.toNonEmptyListOrNull()
    }

    override suspend fun remove(messageId: MessageId) {
        sqlQueryFactory.delete(messageQueue)
            .where(messageQueue.messageId.eq(messageId.value))
            .execute()
    }

    private suspend fun findOne(messageId: MessageId): Either<String, QueuedMessage> {
        return (sqlQueryFactory.select(messageQueue)
            .from(messageQueue)
            .where(messageQueue.messageId.eq(messageId.value))
            .fetch()
            .firstOrNull()
            ?.right() ?: "Unable to find a queued message with id: $messageId".left())
            .map { bean ->
                QueuedMessage(
                    MessageId(bean.messageId),
                    UserId(bean.sender),
                    UserId(bean.recipient),
                    bean.timeOfServerArrival,
                    bean.payload.toString(Charsets.UTF_8)
                )
            }
    }
}

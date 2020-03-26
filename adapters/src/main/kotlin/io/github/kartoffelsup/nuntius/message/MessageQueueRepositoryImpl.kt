package io.github.kartoffelsup.nuntius.message

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.Option
import arrow.core.Tuple2
import arrow.core.Tuple3
import arrow.core.extensions.list.foldable.firstOption
import arrow.core.toT
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

    override suspend fun save(idArrivalMessage: Tuple3<MessageId, ZonedDateTime, Message>): Either<String, QueuedMessage> {
        val (messageId, arrival, message) = idArrivalMessage
        sqlQueryFactory.insert(messageQueue)
            .set(messageQueue.messageId, messageId.value)
            .set(messageQueue.sender, message.sender.uuid.value)
            .set(messageQueue.recipient, message.recipient.uuid.value)
            .set(messageQueue.timeOfServerArrival, arrival)
            .set(
                messageQueue.payload,
                stringFormat.stringify(Message.serializer(), message).toByteArray(Charsets.UTF_8)
            )
            .execute()
        return findOne(messageId)
    }

    override suspend fun findQueuedMessages(userId: UserId): Option<NonEmptyList<Tuple2<MessageId, Message>>> {
        val messages = sqlQueryFactory.select(messageQueue)
            .from(messageQueue)
            .where(messageQueue.recipient.eq(userId.value))
            .fetch()
            .map {
                MessageId(it.messageId) toT stringFormat.parse(
                    Message.serializer(),
                    it.payload.toString(Charsets.UTF_8)
                )
            }

        return NonEmptyList.fromList(messages)
    }

    override suspend fun remove(messageId: MessageId) {
        sqlQueryFactory.delete(messageQueue)
            .where(messageQueue.messageId.eq(messageId.value))
            .execute()
    }

    private suspend fun findOne(messageId: MessageId): Either<String, QueuedMessage> {
        return sqlQueryFactory.select(messageQueue)
            .from(messageQueue)
            .where(messageQueue.messageId.eq(messageId.value))
            .fetch()
            .firstOption()
            .map { bean ->
                QueuedMessage(
                    MessageId(bean.messageId),
                    UserId(bean.sender),
                    UserId(bean.recipient),
                    bean.timeOfServerArrival,
                    bean.payload.toString(Charsets.UTF_8)
                )
            }
            .toEither { "Unable to find a queued message with id: $messageId" }
    }
}

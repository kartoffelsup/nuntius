package io.github.kartoffelsup.nuntius.message

import arrow.core.NonEmptyList
import arrow.core.Option
import io.github.kartoffelsup.nuntius.dtos.Message
import io.github.kartoffelsup.nuntius.dtos.MessageId
import io.github.kartoffelsup.nuntius.dtos.UserId
import io.github.kartoffelsup.nuntius.ports.provided.MessageQueueService
import io.github.kartoffelsup.nuntius.ports.required.MessageQueueRepository
import java.time.ZonedDateTime

class MessageQueueServiceImpl(private val messageQueueRepository: MessageQueueRepository) : MessageQueueService {
    override suspend fun enqueue(message: Triple<MessageId, ZonedDateTime, Message>) {
        messageQueueRepository.save(message)
    }

    override suspend fun findQueuedMessages(userId: UserId): Option<NonEmptyList<Pair<MessageId, Message>>> {
        return messageQueueRepository.findQueuedMessages(userId)
    }

    override suspend fun remove(messageId: MessageId) {
        messageQueueRepository.remove(messageId)
    }
}

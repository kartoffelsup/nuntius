package io.github.kartoffelsup.nuntius.message

import arrow.core.Tuple3
import io.github.kartoffelsup.nuntius.dtos.Message
import io.github.kartoffelsup.nuntius.dtos.MessageId
import io.github.kartoffelsup.nuntius.ports.provided.MessageQueueService
import io.github.kartoffelsup.nuntius.ports.required.MessageQueueRepository
import java.time.ZonedDateTime

class MessageQueueServiceImpl(private val messageQueueRepository: MessageQueueRepository) : MessageQueueService {
    override suspend fun enqueue(message: Tuple3<MessageId, ZonedDateTime, Message>) {
        messageQueueRepository.save(message)
    }
}

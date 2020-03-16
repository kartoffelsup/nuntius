package io.github.kartoffelsup.nuntius.ports.provided

import arrow.core.Tuple2
import arrow.core.Tuple3
import io.github.kartoffelsup.nuntius.dtos.Message
import io.github.kartoffelsup.nuntius.dtos.MessageId
import java.time.ZonedDateTime

interface MessageQueueService {
    suspend fun enqueue(message: Tuple3<MessageId, ZonedDateTime, Message>)
}
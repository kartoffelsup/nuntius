package io.github.kartoffelsup.nuntius.ports.provided

import io.github.kartoffelsup.nuntius.dtos.Message
import io.github.kartoffelsup.nuntius.dtos.MessageId

interface MessageService {
    suspend fun sendMessage(message: Message): MessageId
}

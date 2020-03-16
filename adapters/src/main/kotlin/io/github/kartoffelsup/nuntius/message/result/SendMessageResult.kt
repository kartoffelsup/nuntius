package io.github.kartoffelsup.nuntius.message.result

import io.github.kartoffelsup.nuntius.dtos.MessageId
import kotlinx.serialization.Serializable

@Serializable
data class SendMessageResult(val messageId: MessageId)
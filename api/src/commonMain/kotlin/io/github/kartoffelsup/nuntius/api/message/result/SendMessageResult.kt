package io.github.kartoffelsup.nuntius.api.message.result

import kotlinx.serialization.Serializable

@Serializable
data class SendMessageResult(val messageId: String)
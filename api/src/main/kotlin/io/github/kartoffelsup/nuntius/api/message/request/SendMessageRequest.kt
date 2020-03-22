package io.github.kartoffelsup.nuntius.api.message.request

import kotlinx.serialization.Serializable

@Serializable
data class SendMessageRequest(
    val text: String,
    val recipient: String,
    val sendTimestamp: String
)

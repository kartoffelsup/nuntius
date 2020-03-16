package io.github.kartoffelsup.nuntius.dtos

import kotlinx.serialization.Serializable

@Serializable
data class Attachment(
    val data: String,
    val mimeType: String
)
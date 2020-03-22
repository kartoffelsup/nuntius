package io.github.kartoffelsup.nuntius.api.user.result

import kotlinx.serialization.Serializable

@Serializable
data class UserContacts(
    val contacts: List<UserContact>
)

@Serializable
data class UserContact(
    val userId: String,
    val username: String
)

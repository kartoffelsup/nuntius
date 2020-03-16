package io.github.kartoffelsup.nuntius.ports.required

import io.github.kartoffelsup.nuntius.dtos.NotificationToken

interface NotificationClient {
    suspend fun notify(token: NotificationToken, payload: Map<String, String>)
}
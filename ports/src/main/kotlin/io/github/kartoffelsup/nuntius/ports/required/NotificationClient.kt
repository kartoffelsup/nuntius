package io.github.kartoffelsup.nuntius.ports.required

import io.github.kartoffelsup.nuntius.dtos.NotificationToken
import io.github.kartoffelsup.nuntius.dtos.NuntiusNotification

interface NotificationClient {
    suspend fun notify(token: NotificationToken, notification: NuntiusNotification)
}
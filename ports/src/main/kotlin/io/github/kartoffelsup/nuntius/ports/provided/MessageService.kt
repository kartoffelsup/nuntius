package io.github.kartoffelsup.nuntius.ports.provided

import io.github.kartoffelsup.nuntius.dtos.Message
import io.github.kartoffelsup.nuntius.dtos.MessageId
import io.github.kartoffelsup.nuntius.events.NotificationTokenRegisteredEvent

interface MessageService {
    suspend fun sendMessage(message: Message): MessageId
    suspend fun onNotificationRegistration(notificationTokenRegisteredEvent: NotificationTokenRegisteredEvent): Unit
}

package io.github.kartoffelsup.nuntius.message

import arrow.core.Tuple3
import io.github.kartoffelsup.nuntius.dtos.Message
import io.github.kartoffelsup.nuntius.dtos.MessageId
import io.github.kartoffelsup.nuntius.dtos.MessageNotification
import io.github.kartoffelsup.nuntius.events.NotificationTokenRegisteredEvent
import io.github.kartoffelsup.nuntius.ports.provided.MessageQueueService
import io.github.kartoffelsup.nuntius.ports.provided.MessageService
import io.github.kartoffelsup.nuntius.ports.provided.UserService
import io.github.kartoffelsup.nuntius.ports.required.NotificationClient
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.UUID

class MessageServiceImpl(
    private val userService: UserService,
    private val notificationClient: NotificationClient,
    private val messageQueueService: MessageQueueService
) : MessageService {

    override suspend fun sendMessage(message: Message): MessageId {
        val tokenOfRecipient = userService.findToken(message.recipient.uuid)
        val notification = MessageNotification(message.sender.uuid.value)
        val messageId = MessageId(UUID.randomUUID().toString())
        tokenOfRecipient.fold(
            ifLeft = {
                val timestamp = ZonedDateTime.now(ZoneOffset.UTC)
                messageQueueService.enqueue(Tuple3(messageId, timestamp, message))
            },
            ifRight = { token -> notificationClient.notify(token, notification) }
        )
        return messageId
    }

    override suspend fun onNotificationRegistration(notificationTokenRegisteredEvent: NotificationTokenRegisteredEvent) {
        val user = notificationTokenRegisteredEvent.notificationToken.userId
        val messages = messageQueueService.findQueuedMessages(user)
        messages.fold({}, {
            it.toList().forEach { idToMessage ->
                sendMessage(idToMessage.b)
                messageQueueService.remove(idToMessage.a)
            }
        })
    }
}

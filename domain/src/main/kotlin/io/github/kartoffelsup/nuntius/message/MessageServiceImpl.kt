package io.github.kartoffelsup.nuntius.message

import arrow.core.Tuple3
import io.github.kartoffelsup.nuntius.dtos.Message
import io.github.kartoffelsup.nuntius.dtos.MessageId
import io.github.kartoffelsup.nuntius.events.NotificationTokenRegisteredEvent
import io.github.kartoffelsup.nuntius.events.NuntiusEventBus
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
    private val messageQueueService: MessageQueueService,
    eventBus: NuntiusEventBus
) : MessageService {

    init {
        eventBus.listen(NotificationTokenRegisteredEvent::class, this::onNotificationRegistration)
    }

    override suspend fun sendMessage(message: Message): MessageId {
        val tokenOfRecipient = userService.findToken(message.recipient.uuid)
        val payload = mapOf(
            "userMessage" to message.data,
            "sender" to message.sender.uuid.value
        )
        val messageId = MessageId(UUID.randomUUID().toString())
        tokenOfRecipient.fold(
            ifLeft = {
                val timestamp = ZonedDateTime.now(ZoneOffset.UTC)
                messageQueueService.enqueue(Tuple3(messageId, timestamp, message))
            },
            ifRight = { token -> notificationClient.notify(token, payload) }
        )
        return messageId
    }

    fun onNotificationRegistration(event: NotificationTokenRegisteredEvent) {
        val token = event.notificationToken.token
        val user = event.notificationToken.userId
        println("$user, $token")
    }
}

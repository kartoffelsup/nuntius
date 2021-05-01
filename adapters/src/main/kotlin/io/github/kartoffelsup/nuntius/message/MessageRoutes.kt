package io.github.kartoffelsup.nuntius.message

import arrow.core.computations.either
import io.github.kartoffelsup.nuntius.NuntiusException
import io.github.kartoffelsup.nuntius.api.message.request.SendMessageRequest
import io.github.kartoffelsup.nuntius.api.message.result.SendMessageResult
import io.github.kartoffelsup.nuntius.dtos.Message
import io.github.kartoffelsup.nuntius.dtos.User
import io.github.kartoffelsup.nuntius.dtos.UserId
import io.github.kartoffelsup.nuntius.ports.provided.MessageService
import io.github.kartoffelsup.nuntius.ports.provided.UserService
import io.github.kartoffelsup.nuntius.postIO
import io.github.kartoffelsup.nuntius.userId
import io.ktor.routing.Route
import io.ktor.routing.route

fun Route.message(userService: UserService, messageService: MessageService) {
    route("message") {
        postIO(
            requestSerializer = SendMessageRequest.serializer(),
            resultSerializer = SendMessageResult.serializer(),
            body = { request, call ->
                either {
                    val senderId = userId(call).bind()
                    val recipientId = request.recipient
                    val sender: User = userService.findUser(senderId)
                        .mapLeft { NuntiusException.NotFoundException(it) }
                        .bind()
                    val recipient: User = userService.findUser(UserId(recipientId))
                        .mapLeft { NuntiusException.NotFoundException(it) }
                        .bind()
                    val message = Message(
                        request.text,
                        sender,
                        recipient,
                        sendTimestamp = null,
                        receiveTimestamp = null,
                        deliveryTimestamp = null,
                        attachments = emptyList()
                    )
                    val messageId = messageService.sendMessage(message)
                    SendMessageResult(messageId.value)
                }
            }
        )
    }
}

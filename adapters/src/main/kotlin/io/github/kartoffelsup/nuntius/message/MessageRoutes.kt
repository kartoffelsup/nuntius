package io.github.kartoffelsup.nuntius.message

import arrow.fx.IO
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
                val senderId = userId(call)

                val recipientId = request.recipient
                val sender = !effect { userService.findUser(senderId) }
                    .flatMap { senderEither ->
                        senderEither.fold(
                            ifLeft = { IO.raiseError<User>(NuntiusException.NotFoundException(it)) },
                            ifRight = { IO.just(it) })
                    }

                val recipient = !effect { userService.findUser(UserId(recipientId)) }
                    .flatMap { recipientEither ->
                        recipientEither.fold(
                            ifLeft = { IO.raiseError<User>(NuntiusException.NotFoundException(it)) },
                            ifRight = { IO.just(it) })
                    }

                val message = Message(
                    request.text,
                    sender,
                    recipient,
                    sendTimestamp = null,
                    receiveTimestamp = null,
                    deliveryTimestamp = null,
                    attachments = emptyList()
                )

                val messageId = !effect { messageService.sendMessage(message) }

                SendMessageResult(messageId.value)
            }
        )
    }
}

package io.github.kartoffelsup.nuntius

import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.typeclasses.ConcurrentSyntax
import io.github.kartoffelsup.nuntius.message.message
import io.github.kartoffelsup.nuntius.ports.provided.MessageService
import io.github.kartoffelsup.nuntius.ports.provided.UserService
import io.github.kartoffelsup.nuntius.user.user
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.routing
import kotlinx.serialization.KSerializer
import org.apache.http.HttpStatus

fun Application.routes(userService: UserService, messageService: MessageService) {
    routing {
        user(userService)
        authenticate {
            message(userService, messageService)
        }
    }
}

fun <A, B> Route.postIO(
    path: String = "",
    requestSerializer: KSerializer<A>,
    resultSerializer: KSerializer<B>,
    body: suspend ConcurrentSyntax<ForIO>.(A, ApplicationCall) -> B
) {
    post(path) {
        IO.fx {
                val requestBean: A = !effect { json.parse(requestSerializer, call.receiveText()) }
                body(requestBean, call)
            }
            .map { json.stringify(resultSerializer, it) }
            .attempt()
            .flatMap {
                IO {
                    it.fold(
                        // TODO IO<E, A> in arrow 0.11?
                        ifLeft = {
                            val (status, message) = when (it) {
                                is NuntiusException -> it.statusCode to it.message
                                is IllegalArgumentException -> HttpStatusCode.BadRequest to (it.message ?: "")
                                else -> {
                                    it.printStackTrace()
                                    HttpStatusCode.InternalServerError to "Internal Error. Contact the Administrators."}
                            }
                            call.respondText(
                                status = status,
                                text = message
                            )
                        },
                        ifRight = { call.respondText(it) }
                    )
                }
            }
            .suspended()
    }
}

sealed class NuntiusException(val statusCode: HttpStatusCode) : RuntimeException() {
    abstract override val message: String

    class NotFoundException(override val message: String) : NuntiusException(statusCode = HttpStatusCode.NotFound)
    class NotAuthorizedException(override val message: String): NuntiusException(statusCode = HttpStatusCode.Unauthorized)
}

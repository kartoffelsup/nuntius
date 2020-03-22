package io.github.kartoffelsup.nuntius

import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.typeclasses.ConcurrentSyntax
import io.github.kartoffelsup.nuntius.dtos.UserId
import io.github.kartoffelsup.nuntius.message.message
import io.github.kartoffelsup.nuntius.ports.provided.MessageService
import io.github.kartoffelsup.nuntius.ports.provided.UserService
import io.github.kartoffelsup.nuntius.user.user
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import kotlinx.serialization.KSerializer
import java.io.PipedOutputStream

fun Application.routes(userService: UserService, messageService: MessageService) {
    routing {
        user(userService)
        authenticate {
            message(userService, messageService)
        }
    }
}

suspend fun ConcurrentSyntax<ForIO>.principal(call: ApplicationCall): JWTPrincipal {
    // TODO: IO<E,A>
    val principal: JWTPrincipal = !effect { call.authentication.principal<JWTPrincipal>() }
        ?: !raiseError<JWTPrincipal>(NuntiusException.NotAuthorizedException("Unauthorized."))
    return principal
}

suspend fun ConcurrentSyntax<ForIO>.userId(call: ApplicationCall): UserId {
    val principal = principal(call)
    return principal.payload.getClaim("id").asString()?.let { UserId(it) }
        ?: raiseError<UserId>(NuntiusException.NotAuthorizedException("Invalid token.")).bind()
}

fun <A, B> Route.postIO(
    path: String = "",
    requestSerializer: KSerializer<A>,
    resultSerializer: KSerializer<B>,
    body: suspend ConcurrentSyntax<ForIO>.(A, ApplicationCall) -> B
) {
    routeIO(path,
        requestSerializer,
        resultSerializer,
        method = HttpMethod.Post,
        body = {request, call ->
            request?.let {
                body(request, call)
            } ?: !raiseError<B>(IllegalStateException("Request was null."))
        }
    )
}

fun <B> Route.getIO(
    path: String = "",
    resultSerializer: KSerializer<B>,
    body: suspend ConcurrentSyntax<ForIO>.(ApplicationCall) -> B
) {
    routeIO(path,
        requestSerializer = null,
        method= HttpMethod.Get,
        resultSerializer = resultSerializer,
        body = {_: Unit?, call -> body(call)}
    )
}

private inline fun <A, B> Route.routeIO(
    path: String = "",
    requestSerializer: KSerializer<A>?,
    resultSerializer: KSerializer<B>,
    method: HttpMethod,
    crossinline body: suspend ConcurrentSyntax<ForIO>.(A?, ApplicationCall) -> B
) {
    route(path, method) {
        handle {
            IO.fx {
                    val requestBean = requestSerializer?.let {
                        !effect { json.parse(requestSerializer, call.receiveText()) }
                    }
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
                                call.respond(status, message)
                            },
                            ifRight = { call.respondText(it) }
                        )
                    }
                }
                .suspended()
        }
    }
}

sealed class NuntiusException(val statusCode: HttpStatusCode) : RuntimeException() {
    abstract override val message: String

    class NotFoundException(override val message: String) : NuntiusException(statusCode = HttpStatusCode.NotFound)
    class NotAuthorizedException(override val message: String): NuntiusException(statusCode = HttpStatusCode.Unauthorized)
}

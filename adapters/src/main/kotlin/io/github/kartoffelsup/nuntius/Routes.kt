package io.github.kartoffelsup.nuntius

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.flatten
import arrow.core.rightIfNotNull
import io.github.kartoffelsup.nuntius.dtos.UserId
import io.github.kartoffelsup.nuntius.message.message
import io.github.kartoffelsup.nuntius.ports.provided.MessageService
import io.github.kartoffelsup.nuntius.ports.provided.UserService
import io.github.kartoffelsup.nuntius.user.user
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.KSerializer

fun Application.routes(userService: UserService, messageService: MessageService) {
    routing {
        route("/api/") {
            user(userService)
            authenticate {
                message(userService, messageService)
            }
        }
    }
}

fun extractPrincipal(call: ApplicationCall): Either<NuntiusException, JWTPrincipal> {
    return call.authentication.principal<JWTPrincipal>()
        .rightIfNotNull { NuntiusException.NotAuthorizedException("Unauthorized.") }
}

suspend fun userId(call: ApplicationCall): Either<NuntiusException, UserId> {
    return extractPrincipal(call).flatMap { principal: JWTPrincipal ->
        principal.payload.getClaim("id").asString()?.let { UserId(it) }
            .rightIfNotNull { NuntiusException.NotAuthorizedException("Invalid token.") }
    }
}

fun <A, B> Route.postIO(
    path: String = "",
    requestSerializer: KSerializer<A>,
    resultSerializer: KSerializer<B>,
    body: suspend (A, ApplicationCall) -> Either<Throwable, B>
) {
    routeIO(path,
        requestSerializer,
        resultSerializer,
        method = HttpMethod.Post,
        body = { request: A?, call: ApplicationCall ->
            request?.let {
                body(request, call)
            }.rightIfNotNull { IllegalStateException("Request was null.") }
                .flatten()
        }
    )
}

fun <B> Route.getIO(
    path: String = "",
    resultSerializer: KSerializer<B>,
    body: suspend (ApplicationCall) -> Either<Throwable, B>
) {
    routeIO(path,
        requestSerializer = null,
        method = HttpMethod.Get,
        resultSerializer = resultSerializer,
        body = { _: Unit?, call -> body(call) }
    )
}

private inline fun <A, B> Route.routeIO(
    path: String = "",
    requestSerializer: KSerializer<A>?,
    resultSerializer: KSerializer<B>?,
    method: HttpMethod,
    crossinline body: suspend (A?, ApplicationCall) -> Either<Throwable, B>
) {
    route(path, method) {
        handle {
            val requestBean: Either<Throwable, A?> = requestSerializer?.let {
                call.receiveText().takeIf { it.isNotBlank() }
                    ?.let { Either.catch { json.decodeFromString(requestSerializer, it) } }
                    .rightIfNotNull { IllegalArgumentException("A valid json body is required.") }
                    .flatten()
            } ?: Either.Right(null)

            requestBean.flatMap { body(it, call) }
                .map { resultBody -> resultSerializer?.let { json.encodeToString(resultSerializer, resultBody) } }
                .fold(
                    ifLeft = {
                        val (status, message) = when (it) {
                            is NuntiusException -> it.statusCode to it.message
                            is IllegalArgumentException -> HttpStatusCode.BadRequest to (it.message ?: "")
                            else -> {
                                it.printStackTrace()
                                HttpStatusCode.InternalServerError to "Internal Error. Contact the Administrators."
                            }
                        }
                        call.respond(status, message)
                    },
                    ifRight = { it?.let { call.respondText(it) } }
                )
        }
    }
}

sealed class NuntiusException(val statusCode: HttpStatusCode) : RuntimeException() {
    abstract override val message: String

    class NotFoundException(override val message: String) : NuntiusException(statusCode = HttpStatusCode.NotFound)
    class NotAuthorizedException(override val message: String) :
        NuntiusException(statusCode = HttpStatusCode.Unauthorized)
}

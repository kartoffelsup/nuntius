package io.github.kartoffelsup.nuntius.user

import arrow.core.Tuple2
import arrow.core.Tuple3
import arrow.core.toT
import arrow.fx.IO
import io.github.kartoffelsup.nuntius.NuntiusException
import io.github.kartoffelsup.nuntius.createJwt
import io.github.kartoffelsup.nuntius.dtos.Email
import io.github.kartoffelsup.nuntius.dtos.Password
import io.github.kartoffelsup.nuntius.dtos.UserId
import io.github.kartoffelsup.nuntius.dtos.Username
import io.github.kartoffelsup.nuntius.ports.provided.UserService
import io.github.kartoffelsup.nuntius.postIO
import io.github.kartoffelsup.nuntius.user.request.CreateUserRequest
import io.github.kartoffelsup.nuntius.user.request.LoginRequest
import io.github.kartoffelsup.nuntius.user.request.UpdateNotificationTokenRequest
import io.github.kartoffelsup.nuntius.user.result.CreateUserResult
import io.github.kartoffelsup.nuntius.user.result.SuccessfulLogin
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.routing.Route
import io.ktor.routing.route
import kotlinx.serialization.builtins.serializer

fun Route.user(userService: UserService) {
    route("user") {
        postIO(
            path = "/login",
            requestSerializer = LoginRequest.serializer(),
            resultSerializer = SuccessfulLogin.serializer()
        ) { loginRequest, call ->
            val authRequest = Tuple2(
                Password(loginRequest.password.toByteArray(Charsets.UTF_8)),
                Email(loginRequest.email)
            )
            effect { userService.authenticate(authRequest) }
                .map { authUserEither -> authUserEither.map { createJwt(it) } }
                .map { tokenEither ->
                    tokenEither.map {
                        SuccessfulLogin(
                            it.value
                        )
                    }
                }
                .flatMap { successFulLoginEither ->
                    successFulLoginEither.fold(
                        ifLeft = { IO.raiseError<SuccessfulLogin>(NuntiusException.NotFoundException(it)) },
                        ifRight = { IO.just(it) })
                }.bind()
        }

        postIO(
            requestSerializer = CreateUserRequest.serializer(),
            resultSerializer = CreateUserResult.serializer()
        ) { createUserRequest, call ->
            val request = Tuple3(
                Password(createUserRequest.password.toByteArray()),
                Email(createUserRequest.email),
                Username(createUserRequest.username)
            )
            effect { userService.createUser(request) }.bind()
                .map { CreateUserResult(it.uuid.value) }
                .fold(
                    ifLeft = { IO.raiseError<CreateUserResult>(NuntiusException.NotFoundException(it)) },
                    ifRight = { IO.just(it) }
                ).bind()
        }

        authenticate {
            postIO(
                "/notification-token",
                requestSerializer = UpdateNotificationTokenRequest.serializer(),
                resultSerializer = String.serializer()
            ) { notificationTokenRequest, call ->
                // TODO: IO<E,A>
                val principal: JWTPrincipal = !effect { call.authentication.principal<JWTPrincipal>() }
                    ?: !raiseError<JWTPrincipal>(NuntiusException.NotAuthorizedException("Unauthorized."))

                val userId = !effect { principal.payload.getClaim("id").asString() }
                effect { userService.updateToken(UserId(userId) toT notificationTokenRequest.token) }.bind()
                    .fold(ifLeft = {
                        IO.raiseError<String>(NuntiusException.NotFoundException(it))
                    }, ifRight = {
                        IO.just("Success.")
                    }).bind()
            }
        }
    }
}

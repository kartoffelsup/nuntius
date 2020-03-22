package io.github.kartoffelsup.nuntius.user

import arrow.core.Tuple2
import arrow.core.Tuple3
import arrow.core.getOrElse
import arrow.core.toT
import arrow.fx.IO
import io.github.kartoffelsup.nuntius.NuntiusException
import io.github.kartoffelsup.nuntius.api.user.request.CreateUserRequest
import io.github.kartoffelsup.nuntius.api.user.request.LoginRequest
import io.github.kartoffelsup.nuntius.api.user.request.UpdateNotificationTokenRequest
import io.github.kartoffelsup.nuntius.api.user.result.CreateUserResult
import io.github.kartoffelsup.nuntius.api.user.result.SuccessfulLogin
import io.github.kartoffelsup.nuntius.api.user.result.UserContact
import io.github.kartoffelsup.nuntius.api.user.result.UserContacts
import io.github.kartoffelsup.nuntius.createJwt
import io.github.kartoffelsup.nuntius.dtos.Email
import io.github.kartoffelsup.nuntius.dtos.Password
import io.github.kartoffelsup.nuntius.dtos.Username
import io.github.kartoffelsup.nuntius.getIO
import io.github.kartoffelsup.nuntius.ports.provided.UserService
import io.github.kartoffelsup.nuntius.postIO
import io.github.kartoffelsup.nuntius.userId
import io.ktor.auth.authenticate
import io.ktor.routing.Route
import io.ktor.routing.route
import kotlinx.serialization.builtins.serializer

fun Route.user(userService: UserService) {
    route("user") {
        postIO(
            path = "/login",
            requestSerializer = LoginRequest.serializer(),
            resultSerializer = SuccessfulLogin.serializer()
        ) { loginRequest, _ ->
            val authRequest = Tuple2(
                Password(loginRequest.password.toByteArray(Charsets.UTF_8)),
                Email(loginRequest.email)
            )
            effect { userService.authenticate(authRequest) }
                .map { authUserEither -> authUserEither.map { it toT createJwt(it) } }
                .map { userToToken ->
                    userToToken.map {
                        SuccessfulLogin(it.a.user.uuid.value, it.a.user.username.value, it.b.value)
                    }
                }
                .flatMap { successFulLoginEither ->
                    successFulLoginEither.fold(
                        ifLeft = { IO.raiseError<SuccessfulLogin>(NuntiusException.NotAuthorizedException(it)) },
                        ifRight = { IO.just(it) })
                }.bind()
        }

        postIO(
            requestSerializer = CreateUserRequest.serializer(),
            resultSerializer = CreateUserResult.serializer()
        ) { createUserRequest, _ ->
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
                val userId = userId(call)

                effect { userService.updateToken(userId toT notificationTokenRequest.token) }.bind()
                    .fold(ifLeft = {
                        IO.raiseError<String>(NuntiusException.NotFoundException(it))
                    }, ifRight = {
                        IO.just("Success.")
                    }).bind()
            }

            getIO(
                "/contacts",
                UserContacts.serializer()
            ) { call ->
                val userId = userId(call)
                val contacts = !effect { userService.findContacts(userId) }
                UserContacts(contacts
                    .map { it.toList() }
                    .getOrElse { listOf() }
                    .map {
                        UserContact(it.user.uuid.value, it.user.username.value)
                    }
                )
            }
        }
    }
}

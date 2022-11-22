package io.github.kartoffelsup.nuntius.user

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrElse
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
import io.github.kartoffelsup.nuntius.dtos.UserId
import io.github.kartoffelsup.nuntius.dtos.Username
import io.github.kartoffelsup.nuntius.getIO
import io.github.kartoffelsup.nuntius.ports.provided.UserService
import io.github.kartoffelsup.nuntius.postIO
import io.github.kartoffelsup.nuntius.userId
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import kotlinx.serialization.builtins.serializer

fun Route.user(userService: UserService) {
    route("user") {
        postIO(
            path = "/login",
            requestSerializer = LoginRequest.serializer(),
            resultSerializer = SuccessfulLogin.serializer()
        ) { loginRequest, _ ->
            val authRequest = Pair(
                Password(loginRequest.password.toByteArray(Charsets.UTF_8)),
                Email(loginRequest.email)
            )
            userService.authenticate(authRequest)
                .map { it to createJwt(it) }
                .map {
                    SuccessfulLogin(it.first.user.uuid.value, it.first.user.username.value, it.second.value)
                }
                .mapLeft { NuntiusException.NotAuthorizedException(it) }
        }

        postIO(
            requestSerializer = CreateUserRequest.serializer(),
            resultSerializer = CreateUserResult.serializer()
        ) { createUserRequest, _ ->
            val request = Triple(
                Password(createUserRequest.password.toByteArray()),
                Email(createUserRequest.email),
                Username(createUserRequest.username)
            )
            userService.createUser(request)
                .map { CreateUserResult(it.uuid.value) }
                .mapLeft { NuntiusException.NotFoundException(it) }
        }

        authenticate {
            postIO(
                "/notification-token",
                requestSerializer = UpdateNotificationTokenRequest.serializer(),
                resultSerializer = String.serializer()
            ) { notificationTokenRequest, call ->
                val userId: Either<NuntiusException, UserId> = userId(call)
                userId.flatMap {
                    userService.updateToken(it to notificationTokenRequest.token)
                        .mapLeft { message: String ->
                            NuntiusException.NotFoundException(message)
                        }
                }.map { "Success" }
            }

            getIO(
                "/contacts",
                UserContacts.serializer()
            ) { call ->
                val userId: Either<NuntiusException, UserId> = userId(call)
                userId.map {
                    UserContacts(userService.findContacts(it).map { it.toList() }
                        .getOrElse { listOf() }
                        .map {
                            UserContact(it.user.uuid.value, it.user.username.value)
                        }
                    )
                }
            }
        }
    }
}

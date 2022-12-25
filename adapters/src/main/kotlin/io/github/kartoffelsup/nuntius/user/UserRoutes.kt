package io.github.kartoffelsup.nuntius.user

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
                .mapLeft { NuntiusException.UserExistsException(it) }
        }

        authenticate {
            postIO(
                "/notification-token",
                requestSerializer = UpdateNotificationTokenRequest.serializer(),
                resultSerializer = String.serializer()
            ) { notificationTokenRequest, call ->
                userId(call).flatMap {userId ->
                    userService.updateToken(userId to notificationTokenRequest.token)
                        .mapLeft { message: String ->
                            NuntiusException.NotFoundException(message)
                        }
                }.map { "Success" }
            }

            getIO(
                "/contacts",
                UserContacts.serializer()
            ) { call ->
                userId(call).map { userId ->
                    UserContacts(userService.findContacts(userId).map { it.toList() }
                        .getOrElse { listOf() }
                        .map { contact ->
                            UserContact(contact.user.uuid.value, contact.user.username.value)
                        }
                    )
                }
            }
        }
    }
}

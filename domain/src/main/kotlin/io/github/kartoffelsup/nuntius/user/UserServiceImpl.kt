package io.github.kartoffelsup.nuntius.user

import arrow.core.Either
import arrow.core.Tuple2
import arrow.core.Tuple3
import arrow.core.left
import arrow.core.right
import io.github.kartoffelsup.nuntius.Sha512Hashed
import io.github.kartoffelsup.nuntius.dtos.AuthenticatedUser
import io.github.kartoffelsup.nuntius.dtos.Email
import io.github.kartoffelsup.nuntius.dtos.NotificationToken
import io.github.kartoffelsup.nuntius.dtos.Password
import io.github.kartoffelsup.nuntius.dtos.User
import io.github.kartoffelsup.nuntius.dtos.UserId
import io.github.kartoffelsup.nuntius.dtos.Username
import io.github.kartoffelsup.nuntius.events.NotificationTokenRegisteredEvent
import io.github.kartoffelsup.nuntius.events.NuntiusEventBus
import io.github.kartoffelsup.nuntius.ports.provided.UserService
import io.github.kartoffelsup.nuntius.ports.required.UserRepository
import io.github.kartoffelsup.nuntius.sha512
import java.time.ZonedDateTime

class UserServiceImpl(private val eventBus: NuntiusEventBus, private val userRepository: UserRepository) : UserService {
    override suspend fun authenticate(request: Tuple2<Password, Email>): Either<String, AuthenticatedUser> {
        val (providedPassword, email) = request
        val findUser: Either<String, User> = userRepository.findUserByEmail(email)
        return findUser.fold(
            ifLeft = { it.left() },
            ifRight = { dbUser -> authenticate(providedPassword, email, dbUser) }
        )
    }

    private suspend fun authenticate(
        providedPassword: Password,
        providedMail: Email,
        dbUser: User
    ): Either<String, AuthenticatedUser> {
        val hashedPw = hashPw(providedPassword, providedMail)
        return if (dbUser.email.value == providedMail.value && dbUser.password?.value?.contentEquals(hashedPw.payload) == true) {
            val toUpdate = dbUser.copy(lastLogin = ZonedDateTime.now())
            userRepository.updateUser(toUpdate)
                .map { AuthenticatedUser(it, null) }
        } else {
            Either.left("Invalid credentials.")
        }
    }

    override suspend fun createUser(request: Tuple3<Password, Email, Username>): Either<String, User> {
        val (pass, email, username) = request
        val hashedPw: Sha512Hashed = hashPw(pass, email)
        return userRepository.saveUser(Tuple3(Password(hashedPw.payload), email, username))
    }

    override suspend fun updateToken(request: Tuple2<UserId, String>): Either<String, NotificationToken> {
        val (userId, token) = request
        return userRepository.updateToken(userId, token)
            .fold(ifLeft = { it.left() }, ifRight = {
                eventBus.send(NotificationTokenRegisteredEvent(it))
                it.right()
            })
    }

    override suspend fun findUser(userId: UserId): Either<String, User> {
        return userRepository.findUserById(userId)
    }

    override suspend fun findToken(userId: UserId): Either<String, NotificationToken> {
        return userRepository.findUserNotificationToken(userId)
    }

    private fun hashPw(providedPassword: Password, email: Email): Sha512Hashed {
        val middleIndex = providedPassword.value.size / 2
        val left = providedPassword.value.copyOfRange(0, middleIndex)
        val right = providedPassword.value.copyOfRange(middleIndex, providedPassword.value.size - 1)
        val emailBytes = email.value.toByteArray(Charsets.UTF_8)
        return sha512(left + emailBytes + right)
    }
}

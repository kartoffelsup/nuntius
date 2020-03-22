package io.github.kartoffelsup.nuntius.ports.provided

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.Option
import arrow.core.Tuple2
import arrow.core.Tuple3
import io.github.kartoffelsup.nuntius.dtos.AuthenticatedUser
import io.github.kartoffelsup.nuntius.dtos.Contact
import io.github.kartoffelsup.nuntius.dtos.Email
import io.github.kartoffelsup.nuntius.dtos.NotificationToken
import io.github.kartoffelsup.nuntius.dtos.Password
import io.github.kartoffelsup.nuntius.dtos.User
import io.github.kartoffelsup.nuntius.dtos.UserId
import io.github.kartoffelsup.nuntius.dtos.Username

interface UserService {
    suspend fun authenticate(request: Tuple2<Password, Email>): Either<String, AuthenticatedUser>
    suspend fun createUser(request: Tuple3<Password, Email, Username>): Either<String, User>
    suspend fun updateToken(request: Tuple2<UserId, String>): Either<String, NotificationToken>
    suspend fun findUser(userId: UserId): Either<String, User>
    suspend fun findToken(userId: UserId): Either<String, NotificationToken>
    suspend fun findContacts(userId: UserId): Option<NonEmptyList<Contact>>
}

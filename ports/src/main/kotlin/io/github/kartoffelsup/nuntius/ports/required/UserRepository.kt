package io.github.kartoffelsup.nuntius.ports.required

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.Option
import io.github.kartoffelsup.nuntius.dtos.Contact
import io.github.kartoffelsup.nuntius.dtos.Email
import io.github.kartoffelsup.nuntius.dtos.NotificationToken
import io.github.kartoffelsup.nuntius.dtos.Password
import io.github.kartoffelsup.nuntius.dtos.User
import io.github.kartoffelsup.nuntius.dtos.UserId
import io.github.kartoffelsup.nuntius.dtos.Username

interface UserRepository {
    suspend fun findUserById(id: UserId): Either<String, User>
    suspend fun findUserByEmail(email: Email): Either<String, User>
    suspend fun saveUser(pwMailUsername: Triple<Password, Email, Username>): Either<String, User>
    suspend fun findUserNotificationToken(id: UserId): Either<String, NotificationToken>
    suspend fun updateToken(userId: UserId, token: String): Either<String, NotificationToken>
    suspend fun updateUser(userToUpdate: User): Either<String, User>
    suspend fun findContacts(id: UserId): Option<NonEmptyList<Contact>>
}

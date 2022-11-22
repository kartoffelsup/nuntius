package io.github.kartoffelsup.nuntius.user

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.Option
import arrow.core.right
import arrow.core.rightIfNotNull
import arrow.core.toNonEmptyListOrNull
import arrow.core.toOption
import com.querydsl.core.types.dsl.LiteralExpression
import com.querydsl.sql.SQLQueryFactory
import io.github.kartoffelsup.nuntius.dtos.Contact
import io.github.kartoffelsup.nuntius.dtos.Email
import io.github.kartoffelsup.nuntius.dtos.NotificationToken
import io.github.kartoffelsup.nuntius.dtos.Password
import io.github.kartoffelsup.nuntius.dtos.User
import io.github.kartoffelsup.nuntius.dtos.UserId
import io.github.kartoffelsup.nuntius.dtos.Username
import io.github.kartoffelsup.nuntius.ports.required.UserRepository
import io.github.kartoffelsup.nuntius.sql.QNuntiusUser
import io.github.kartoffelsup.nuntius.sql.QNuntiusUser.nuntiusUser
import io.github.kartoffelsup.nuntius.sql.QUserContact.userContact
import io.github.kartoffelsup.nuntius.sql.QUserNotification.userNotification
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

class UserRepositoryImpl(private val sqlQueryFactory: SQLQueryFactory) : UserRepository {
    override suspend fun findUserById(id: UserId): Either<String, User> =
        findUser(nuntiusUser.uuid, id.value)

    override suspend fun findUserByEmail(email: Email): Either<String, User> =
        findUser(nuntiusUser.email, email.value)

    override suspend fun saveUser(pwMailUsername: Triple<Password, Email, Username>): Either<String, User> {
        val (pw, mail, username) = pwMailUsername
        val uuid = UUID.randomUUID().toString()
        sqlQueryFactory.insert(nuntiusUser)
            .set(nuntiusUser.username, username.value)
            .set(nuntiusUser.pw, pw.value)
            .set(nuntiusUser.email, mail.value)
            .set(nuntiusUser.uuid, uuid)
            .set(nuntiusUser.createdAt, ZonedDateTime.now(ZoneOffset.UTC))
            .execute()

        return findUserById(UserId(uuid))
    }

    override suspend fun findUserNotificationToken(id: UserId): Either<String, NotificationToken> {
        return sqlQueryFactory.select(userNotification)
            .from(userNotification)
            .where(userNotification.userId.eq(id.value))
            .fetch()
            .firstOrNull()
            ?.let { NotificationToken(UserId(it.userId), it.token, it.updatedAt) }
            .rightIfNotNull { "Unable to find a Token for user: '$id'" }
    }

    override suspend fun updateToken(userId: UserId, token: String): Either<String, NotificationToken> {
        val existingTokenEither: Either<String, NotificationToken> = findUserNotificationToken(userId)
        val now = ZonedDateTime.now(ZoneOffset.UTC)
        when (existingTokenEither) {
            is Either.Left -> sqlQueryFactory.insert(userNotification)
                .set(userNotification.token, token)
                .set(userNotification.updatedAt, now)
                .set(userNotification.userId, userId.value)
                .execute()
            is Either.Right -> sqlQueryFactory.update(userNotification)
                .set(userNotification.token, token)
                .set(userNotification.updatedAt, now)
                .where(userNotification.userId.eq(userId.value))
                .execute()
        }
        return NotificationToken(userId, token, now).right()
    }

    override suspend fun updateUser(userToUpdate: User): Either<String, User> {
        val update = sqlQueryFactory.update(nuntiusUser)
            .where(nuntiusUser.uuid.eq(userToUpdate.uuid.value))
            .set(nuntiusUser.username, userToUpdate.username.value)

        userToUpdate.lastLogin?.let {
            update.set(nuntiusUser.lastLogin, it)
        }
        update.execute()
        return findUserById(userToUpdate.uuid)
    }

    override suspend fun findContacts(id: UserId): Option<NonEmptyList<Contact>> {
        val contact = QNuntiusUser("contact")
        val result = sqlQueryFactory.select(contact)
            .from(nuntiusUser)
            .innerJoin(userContact)
            .on(userContact.userId.eq(nuntiusUser.uuid))
            .innerJoin(contact)
            .on(userContact.contactId.eq(contact.uuid))
            .where(nuntiusUser.uuid.eq(id.value))
            .fetch()
            .map {
                Contact(User(UserId(it.uuid), Username(it.username), Email(it.email), it.createdAt, it.lastLogin))
            }
        return result.toNonEmptyListOrNull().toOption()
    }

    private suspend fun <T : Comparable<T>> findUser(
        path: LiteralExpression<T>,
        value: T
    ): Either<String, User> {
        return sqlQueryFactory.select(nuntiusUser)
            .from(nuntiusUser)
            .where(path.eq(value))
            .fetch()
            .map {
                User(
                    UserId(it.uuid),
                    Username(it.username),
                    Email(it.email),
                    it.createdAt,
                    it.lastLogin
                ).apply {
                    password = Password(it.pw)
                }
            }
            .firstOrNull()
            .rightIfNotNull { "User with '$path=$value' not found." }
    }
}

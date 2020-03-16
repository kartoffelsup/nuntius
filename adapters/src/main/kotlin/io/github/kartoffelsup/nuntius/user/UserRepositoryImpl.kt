package io.github.kartoffelsup.nuntius.user

import arrow.core.Either
import arrow.core.Tuple3
import arrow.core.extensions.list.foldable.firstOption
import com.querydsl.core.types.dsl.LiteralExpression
import com.querydsl.sql.SQLQueryFactory
import io.github.kartoffelsup.nuntius.dtos.Email
import io.github.kartoffelsup.nuntius.dtos.NotificationToken
import io.github.kartoffelsup.nuntius.dtos.Password
import io.github.kartoffelsup.nuntius.dtos.User
import io.github.kartoffelsup.nuntius.dtos.UserId
import io.github.kartoffelsup.nuntius.dtos.Username
import io.github.kartoffelsup.nuntius.ports.required.UserRepository
import io.github.kartoffelsup.nuntius.sql.QUser.user
import io.github.kartoffelsup.nuntius.sql.QUserNotification.userNotification
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.UUID

class UserRepositoryImpl(private val sqlQueryFactory: SQLQueryFactory) : UserRepository {
    override suspend fun findUserById(id: UserId): Either<String, User> =
        findUser(user.uuid, id.value)

    override suspend fun findUserByEmail(email: Email): Either<String, User> =
        findUser(user.email, email.value)

    override suspend fun saveUser(pwMailUsername: Tuple3<Password, Email, Username>): Either<String, User> {
        val (pw, mail, username) = pwMailUsername
        val uuid = UUID.randomUUID().toString()
        sqlQueryFactory.insert(user)
            .set(user.username, username.value)
            .set(user.pw, pw.value)
            .set(user.email, mail.value)
            .set(user.uuid, uuid)
            .set(user.createdAt, ZonedDateTime.now(ZoneOffset.UTC))
            .execute()

        return findUserById(UserId(uuid))
    }

    override suspend fun findUserNotificationToken(id: UserId): Either<String, NotificationToken> {
        return sqlQueryFactory.select(userNotification)
            .from(userNotification)
            .where(userNotification.userId.eq(id.value))
            .fetch()
            .firstOption()
            .map { NotificationToken(UserId(it.userId), it.token, it.updatedAt) }
            .toEither { "Unable to find a Token for user: '$id'" }
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
        return Either.right(NotificationToken(userId, token, now))
    }

    override suspend fun updateUser(userToUpdate: User): Either<String, User> {
        val update = sqlQueryFactory.update(user)
            .where(user.uuid.eq(userToUpdate.uuid.value))
            .set(user.username, userToUpdate.username.value)

        userToUpdate.lastLogin?.let {
            update.set(user.lastLogin, it)
        }
        update.execute()
        return findUserById(userToUpdate.uuid)
    }

    private suspend fun <T : Comparable<T>> findUser(
        path: LiteralExpression<T>,
        value: T
    ): Either<String, User> {
        return sqlQueryFactory.select(user)
            .from(user)
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
            .firstOption().toEither { "User with '$path=$value' not found." }
    }
}

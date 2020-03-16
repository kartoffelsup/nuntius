package io.github.kartoffelsup.nuntius.dtos

data class AuthenticatedUser(val user: User, val notificationToken: NotificationToken?)

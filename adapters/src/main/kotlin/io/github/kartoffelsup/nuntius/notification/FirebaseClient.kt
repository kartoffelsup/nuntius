package io.github.kartoffelsup.nuntius.notification

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import io.github.kartoffelsup.nuntius.api.notification.MessageNotificationDto
import io.github.kartoffelsup.nuntius.dtos.MessageNotification
import io.github.kartoffelsup.nuntius.dtos.NotificationToken
import io.github.kartoffelsup.nuntius.dtos.NuntiusNotification
import io.github.kartoffelsup.nuntius.json
import io.github.kartoffelsup.nuntius.ports.required.NotificationClient

class FirebaseClient(firebase: FirebaseApp) : NotificationClient {
    private val firebaseMessaging: FirebaseMessaging = FirebaseMessaging.getInstance(firebase)
    override suspend fun notify(token: NotificationToken, notification: NuntiusNotification) {
        val dto: MessageNotificationDto = when (notification) {
            is MessageNotification -> MessageNotificationDto(notification.senderId)
        }
        firebaseMessaging.send(
            Message.builder()
                .putData("data", json.stringify(MessageNotificationDto.serializer(), dto))
                .setToken(token.token)
                .build()
        )
    }
}

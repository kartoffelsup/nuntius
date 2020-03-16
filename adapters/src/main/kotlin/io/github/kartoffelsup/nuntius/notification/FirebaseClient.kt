package io.github.kartoffelsup.nuntius.notification

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import io.github.kartoffelsup.nuntius.dtos.NotificationToken
import io.github.kartoffelsup.nuntius.ports.required.NotificationClient

class FirebaseClient(firebase: FirebaseApp) : NotificationClient {
    private val firebaseMessaging: FirebaseMessaging = FirebaseMessaging.getInstance(firebase)
    override suspend fun notify(token: NotificationToken, payload: Map<String, String>) {
        firebaseMessaging.send(
            Message.builder()
                .putData("test", "test")
                .setToken("test")
                .build()
        )
    }
}

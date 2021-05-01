package io.github.kartoffelsup.nuntius

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.getOrHandle
import com.google.auth.oauth2.GoogleCredentials
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.querydsl.sql.Configuration
import com.querydsl.sql.PostgreSQLTemplates
import com.querydsl.sql.SQLQueryFactory
import com.querydsl.sql.types.JSR310ZonedDateTimeType
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.kartoffelsup.argparsing.ArgParser
import io.github.kartoffelsup.argparsing.ArgParserError
import io.github.kartoffelsup.argparsing.longOption
import io.github.kartoffelsup.argparsing.shortOption
import io.github.kartoffelsup.nuntius.events.NotificationTokenRegisteredEvent
import io.github.kartoffelsup.nuntius.message.MessageQueueRepositoryImpl
import io.github.kartoffelsup.nuntius.message.MessageQueueServiceImpl
import io.github.kartoffelsup.nuntius.message.MessageServiceImpl
import io.github.kartoffelsup.nuntius.notification.FirebaseClient
import io.github.kartoffelsup.nuntius.ports.provided.MessageService
import io.github.kartoffelsup.nuntius.user.UserRepositoryImpl
import io.github.kartoffelsup.nuntius.user.UserServiceImpl
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.features.AutoHeadResponse
import io.ktor.features.CallLogging
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import java.io.FileInputStream
import javax.sql.DataSource
import kotlin.system.exitProcess

suspend fun main(args: Array<String>): Unit {
    val (ds: DataSource, options: FirebaseOptions) = config(args)
    val queryFactory =
        SQLQueryFactory(Configuration(PostgreSQLTemplates()).apply {
            register("user", "created_at", JSR310ZonedDateTimeType())
            register("user", "last_login", JSR310ZonedDateTimeType())
            register("user_notification", "updated_at", JSR310ZonedDateTimeType())
            register("message_queue", "time_of_server_arrival", JSR310ZonedDateTimeType())
        }, ds)


    val firebase = FirebaseApp.initializeApp(options)
    val eventBus = EventBus()

    val producer: suspend (NotificationTokenRegisteredEvent) -> Unit = { eventBus.post(it) }
    val userService = UserServiceImpl(producer, UserRepositoryImpl(queryFactory))
    val messageQueueService = MessageQueueServiceImpl(MessageQueueRepositoryImpl(json, queryFactory))
    val messageService = MessageServiceImpl(userService, FirebaseClient(firebase), messageQueueService)
    eventBus.register(DeliverEvent(messageService))

    embeddedServer(Netty, 8080) {
        install(AutoHeadResponse)
//        install(CORS) {
//            host("localhost:9000")
//            header("content-type")
//        }
        install(Authentication) {
            jwt {
                realm = "nuntius"
                verifier(jwtVerifier)
                validate { credential ->
                    if (credential.payload.audience.contains("nuntius")) JWTPrincipal(credential.payload) else null
                }
            }
        }
        install(CallLogging)
        routes(
            userService,
            messageService
        )
    }.start(wait = true)
}

private suspend fun config(args: Array<String>): Pair<DataSource, FirebaseOptions> {
    val argParser = ArgParser(args)
    val c: Either<ArgParserError, Pair<DataSource, FirebaseOptions>> = either {
        val user = argParser.value("dbu".shortOption(), "database-user".longOption()).bind()
        val db = argParser.value("db".shortOption(), "database".longOption()).bind()
        val dbHost = argParser.value("dbh".shortOption(), "database-host".longOption()).bind()
        val pw = argParser.value("dbpw".shortOption(), "database-password".longOption()).bind()
        val fb = argParser.value("fb".shortOption(), "firebase-config-file".longOption()).bind()
        val fbUrl = argParser.value("fburl".shortOption(), "firebase-url".longOption()).bind()

        val ds = HikariDataSource(HikariConfig().also {
            it.jdbcUrl = "jdbc:postgresql://$dbHost/$db"
            it.username = user
            it.password = pw
            it.initializationFailTimeout = 1
        })

        val serviceAccount = FileInputStream(fb)
        val options = FirebaseOptions.builder()
            .setCredentials(serviceAccount.use { GoogleCredentials.fromStream(it) })
            .setDatabaseUrl(fbUrl)
            .build()

        ds to options
    }
    return c.getOrHandle { err: ArgParserError ->
        System.err.println(err.message)
        exitProcess(1)
    }
}

class DeliverEvent(private val messageService: MessageService) {
    @Subscribe
    fun onNotificationRegistered(notificationTokenRegisteredEvent: NotificationTokenRegisteredEvent) =
        runBlocking { messageService.onNotificationRegistration(notificationTokenRegisteredEvent) }
}


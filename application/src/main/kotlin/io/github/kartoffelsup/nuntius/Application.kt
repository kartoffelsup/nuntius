package io.github.kartoffelsup.nuntius

import arrow.core.Either
import arrow.core.Tuple2
import arrow.core.extensions.either.applicativeError.applicativeError
import arrow.core.extensions.fx
import arrow.core.getOrHandle
import arrow.core.toT
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.extensions.io.concurrent.concurrent
import arrow.fx.extensions.io.dispatchers.dispatchers
import arrow.fx.typeclasses.seconds
import com.google.auth.oauth2.GoogleCredentials
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
import io.github.kartoffelsup.nuntius.events.NuntiusEventBus
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
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.io.FileInputStream
import javax.sql.DataSource
import kotlin.system.exitProcess

suspend fun main(args: Array<String>): Unit = IO.fx {
    val (ds: DataSource, options: FirebaseOptions) = !effect { config(args) }
    val queryFactory = !effect {
        SQLQueryFactory(Configuration(PostgreSQLTemplates()).apply {
            register("user", "created_at", JSR310ZonedDateTimeType())
            register("user", "last_login", JSR310ZonedDateTimeType())
            register("user_notification", "updated_at", JSR310ZonedDateTimeType())
            register("message_queue", "time_of_server_arrival", JSR310ZonedDateTimeType())
        }, ds)
    }

    val firebase = !effect { FirebaseApp.initializeApp(options) }
    val eventBus: NuntiusEventBus<ForIO> = !NuntiusEventBus(IO.concurrent())

    val producer: suspend (NotificationTokenRegisteredEvent) -> Unit = {
        IO.fx {
            eventBus.send(it).bind()
            !effect { println("sent event") }
        }.suspended()
    }

    val userService = UserServiceImpl(producer, UserRepositoryImpl(queryFactory))
    val messageQueueService = MessageQueueServiceImpl(MessageQueueRepositoryImpl(json, queryFactory))
    val messageService = MessageServiceImpl(userService, FirebaseClient(firebase), messageQueueService)

    !deliverEvents(eventBus, messageService).fork(IO.dispatchers().io())

    !effect {
        embeddedServer(Netty, 8080) {
            install(Authentication) {
                jwt {
                    realm = "nuntius"
                    verifier(jwtVerifier)
                    validate { credential ->
                        if (credential.payload.audience.contains("nuntius")) JWTPrincipal(credential.payload) else null
                    }
                }
            }
            routes(
                userService,
                messageService
            )
        }.start(wait = true)
    }
    Unit
}.suspended()

private suspend fun config(args: Array<String>): Tuple2<DataSource, FirebaseOptions> {
    val argParser = ArgParser(Either.applicativeError(), args)
    return Either.fx<ArgParserError, Tuple2<DataSource, FirebaseOptions>> {
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
            it.initializationFailTimeout = 0
        })

        val serviceAccount = FileInputStream(fb)
        val options = FirebaseOptions.Builder()
            .setCredentials(serviceAccount.use { GoogleCredentials.fromStream(it) })
            .setDatabaseUrl(fbUrl)
            .build()

        ds toT options
    }.getOrHandle { err: ArgParserError ->
        System.err.println(err.message)
        exitProcess(1)
    }
}

fun deliverEvents(
    eventBus: NuntiusEventBus<ForIO>,
    consumer: MessageService
): IO<Unit> = IO.fx {
    !effect { println("Start listening") }
    val event: NotificationTokenRegisteredEvent = !eventBus.listen(NotificationTokenRegisteredEvent::class)
    !effect { println("Received event") }
    !effect { consumer.onNotificationRegistration(event) }
    !effect { println("delivered event") }
    !IO.sleep(5.seconds)
    !deliverEvents(eventBus, consumer)
}

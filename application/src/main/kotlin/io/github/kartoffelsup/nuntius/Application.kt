package io.github.kartoffelsup.nuntius

import arrow.core.Either
import arrow.core.Tuple2
import arrow.core.extensions.either.applicativeError.applicativeError
import arrow.core.extensions.fx
import arrow.core.getOrHandle
import arrow.core.toT
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.querydsl.sql.Configuration
import com.querydsl.sql.PostgreSQLTemplates
import com.querydsl.sql.SQLQueryFactory
import com.querydsl.sql.types.JSR310ZonedDateTimeType
import com.zaxxer.hikari.HikariDataSource
import io.github.kartoffelsup.argparsing.ArgParser
import io.github.kartoffelsup.argparsing.ArgParserError
import io.github.kartoffelsup.argparsing.ln
import io.github.kartoffelsup.argparsing.sn
import io.github.kartoffelsup.nuntius.events.NuntiusEventBus
import io.github.kartoffelsup.nuntius.events.NuntiusEventBusImpl
import io.github.kartoffelsup.nuntius.message.MessageQueueRepositoryImpl
import io.github.kartoffelsup.nuntius.message.MessageQueueServiceImpl
import io.github.kartoffelsup.nuntius.message.MessageServiceImpl
import io.github.kartoffelsup.nuntius.notification.FirebaseClient
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

fun main(args: Array<String>) {
    val (ds: DataSource, options: FirebaseOptions) = config(args)

    val queryFactory = SQLQueryFactory(Configuration(PostgreSQLTemplates()).apply {
        register("user", "created_at", JSR310ZonedDateTimeType())
        register("user", "last_login", JSR310ZonedDateTimeType())
        register("user_notification", "updated_at", JSR310ZonedDateTimeType())
        register("message_queue", "time_of_server_arrival", JSR310ZonedDateTimeType())
    }, ds)

    val firebase = FirebaseApp.initializeApp(options)
    val eventBus: NuntiusEventBus = NuntiusEventBusImpl()

    val userService = UserServiceImpl(eventBus, UserRepositoryImpl(queryFactory))
    val messageQueueService = MessageQueueServiceImpl(MessageQueueRepositoryImpl(json, queryFactory))
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
            MessageServiceImpl(userService, FirebaseClient(firebase), messageQueueService, eventBus)
        )
    }.start(wait = true)
}

private fun config(args: Array<String>): Tuple2<DataSource, FirebaseOptions> {
    val argParser = ArgParser(Either.applicativeError(), args)
    return Either.fx<ArgParserError, Tuple2<DataSource, FirebaseOptions>> {
        val user = argParser.value("dbu".sn(), "database-user".ln()).bind()
        val db = argParser.value("db".sn(), "database".ln()).bind()
        val dbHost = argParser.value("dbh".sn(), "database-host".ln()).bind()
        val pw = argParser.value("dbpw".sn(), "database-password".ln()).bind()
        val fb = argParser.value("fb".sn(), "firebase-config-file".ln()).bind()
        val fbUrl = argParser.value("fburl".sn(), "firebase-url".ln()).bind()

        val ds = datasource("jdbc:postgresql://$dbHost/$db", user, pw)
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

private fun datasource(jdbcUrl: String, username: String, password: String): HikariDataSource =
    HikariDataSource().also {
        it.jdbcUrl = jdbcUrl
        it.username = username
        it.password = password
    }

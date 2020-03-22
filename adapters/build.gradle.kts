import com.querydsl.sql.Configuration
import com.querydsl.sql.PostgreSQLTemplates
import com.querydsl.sql.types.JSR310ZonedDateTimeType
import io.github.kartoffelsup.querydsl.sql.codegen.GenerateQueryDslSqlSources
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.postgresql.ds.PGSimpleDataSource

buildscript {
    repositories {
        mavenLocal()
    }

    dependencies {
        val queryDslVersion: String by extra
        val postgresVersion: String by extra

        classpath("io.github.kartoffelsup:querydsl-sql-codegen-gradle-plugin:0.0.3-SNAPSHOT") {
            exclude("com.querydsl", "querydsl-sql-codegen")
        }
        classpath("com.querydsl:querydsl-sql-codegen:$queryDslVersion")
        classpath("org.postgresql:postgresql:$postgresVersion")
        classpath("io.github.kartoffelsup:kotlin-querydsl-sql-codegen:0.0.1-SNAPSHOT")
    }
}

apply {
    plugin("io.github.kartoffelsup.querydsl.sql.codegen")
}

plugins {
    kotlin("plugin.serialization") version "1.3.70"
}

dependencies {
    val arrowVersion: String by extra
    val ktorVersion: String by extra
    val queryDslVersion: String by extra
    val firebaseAdminVersion: String by extra
    val kotlinxSerializationVersion: String by extra
    val javaxAnnotationApiVersion: String by extra

    api("io.arrow-kt:arrow-fx:$arrowVersion")
    api("io.arrow-kt:arrow-syntax:$arrowVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")

    implementation("javax.annotation:javax.annotation-api:$javaxAnnotationApiVersion")

    api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$kotlinxSerializationVersion")
    api("com.google.firebase:firebase-admin:$firebaseAdminVersion") {
        exclude(group = "io.netty")
        exclude(group = "com.fasterxml.jackson.core")
    }
    api("io.ktor:ktor-auth-jwt:$ktorVersion")
    api("com.querydsl:querydsl-sql:$queryDslVersion")
    api(project(":ports"))
    api(project(":api"))
}

val generatedSourcesPath = file("src/generated/kotlin")

sourceSets.main {
    java {
        srcDir(generatedSourcesPath)
    }
}

tasks {
    withType<GenerateQueryDslSqlSources> {
        val user = System.getProperty("db-user")
        val password = System.getProperty("db-pw")
        val host = System.getProperty("db-host")
        val ds: javax.sql.DataSource =
            PGSimpleDataSource().apply {
                this.user = user
                this.password = password
                this.serverNames = arrayOf(host)
            }

        val file = file(generatedSourcesPath)
        target.set(file)
        packageName.set("io.github.kartoffelsup.nuntius.sql")
        beanPackageName.set("io.github.kartoffelsup.nuntius.bean")
        dataSource.set(ds)
        configuration.set(Configuration(PostgreSQLTemplates()).apply {
            register("user", "created_at", JSR310ZonedDateTimeType())
            register("user", "last_login", JSR310ZonedDateTimeType())
            register("user_notification", "updated_at", JSR310ZonedDateTimeType())
            register("message_queue", "time_of_server_arrival", JSR310ZonedDateTimeType())
        })

        customizer.set { this.setBeanSuffix("Bean") }
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
}

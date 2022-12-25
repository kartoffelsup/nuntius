import com.querydsl.sql.Configuration
import com.querydsl.sql.PostgreSQLTemplates
import com.querydsl.sql.types.JSR310ZonedDateTimeType
import io.github.kartoffelsup.querydsl.sql.codegen.GenerateQueryDslSqlSources
import org.postgresql.ds.PGSimpleDataSource

buildscript {
    repositories {
        mavenLocal()
    }

    dependencies {
        val queryDslVersion: String by rootProject.extra
        val postgresVersion: String by rootProject.extra

        classpath("io.github.kartoffelsup:querydsl-sql-codegen-gradle-plugin:0.0.3-SNAPSHOT") {
            exclude("com.querydsl", "querydsl-sql-codegen")
        }
        classpath("com.querydsl:querydsl-sql-codegen:$queryDslVersion")
        classpath("org.postgresql:postgresql:$postgresVersion")
    }
}

apply {
    plugin("io.github.kartoffelsup.querydsl.sql.codegen")
}

plugins {
    kotlin("plugin.serialization") version "1.9.10"
}

dependencies {
    val arrowVersion: String by rootProject.extra
    val ktorVersion: String by rootProject.extra
    val queryDslVersion: String by rootProject.extra
    val firebaseAdminVersion: String by rootProject.extra
    val kotlinxSerializationVersion: String by rootProject.extra
    val javaxAnnotationApiVersion: String by rootProject.extra
    val junitVersion: String by rootProject.extra
    val mockkVersion: String by rootProject.extra

    api("io.arrow-kt:arrow-core:$arrowVersion")
    api("io.arrow-kt:arrow-fx-coroutines:$arrowVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation(kotlin("reflect"))

    implementation("javax.annotation:javax.annotation-api:$javaxAnnotationApiVersion")

    api("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVersion")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$kotlinxSerializationVersion")
    api("com.google.firebase:firebase-admin:$firebaseAdminVersion") {
        exclude(group = "io.netty")
        exclude(group = "com.fasterxml.jackson.core")
    }
    api("io.ktor:ktor-server-auth-jwt-jvm:$ktorVersion")
    api("com.querydsl:querydsl-sql:$queryDslVersion")
    api(project(":ports"))
    api(project(":api"))

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation(project(":client"))
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
            register("nuntius_user", "created_at", JSR310ZonedDateTimeType())
            register("nuntius_user", "last_login", JSR310ZonedDateTimeType())
            register("user_notification", "updated_at", JSR310ZonedDateTimeType())
            register("message_queue", "time_of_server_arrival", JSR310ZonedDateTimeType())

        })

        customizer.set { this.setBeanSuffix("Bean") }
    }
}

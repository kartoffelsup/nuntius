plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.3.70"
    id("maven-publish")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvm {
        mavenPublication {
            artifactId = "nuntius-api-client-jvm"
            groupId = "io.github.kartoffelsup"
            version = "0.0.1-SNAPSHOT"
        }
    }
    js {
        browser()
    }
    sourceSets {
        val kotlinxSerializationVersion: String by rootProject.extra
        val okHttpVersion: String by rootProject.extra
        val coroutinesVersion: String by rootProject.extra

        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$kotlinxSerializationVersion")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$coroutinesVersion")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$kotlinxSerializationVersion")
                implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:$kotlinxSerializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutinesVersion")
            }
        }
    }
}

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version ("1.3.72")
    id("maven-publish")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvm {
        mavenPublication {
            artifactId = "nuntius-api-jvm"
            groupId = "io.github.kartoffelsup"
            version = "0.0.1-SNAPSHOT"
        }
    }
    js {
        browser()
    }
    sourceSets {
        val kotlinxSerializationVersion: String by rootProject.extra

        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$kotlinxSerializationVersion")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$kotlinxSerializationVersion")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:$kotlinxSerializationVersion")
            }
        }
    }
}

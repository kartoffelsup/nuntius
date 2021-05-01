plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version ("1.5.0")
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
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVersion")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
            }
        }
    }
}

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
            artifactId = "nuntius-api-client-jvm"
            groupId = "io.github.kartoffelsup"
            version = "0.0.2-SNAPSHOT"
        }
    }
    js {
        browser()
        useCommonJs()
    }
    sourceSets {
        val kotlinxSerializationVersion: String by rootProject.extra
        val okHttpVersion: String by rootProject.extra
        val coroutinesVersion: String by rootProject.extra
        val kotestVersion: String by rootProject.extra
        val junitVersion: String by rootProject.extra

        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVersion")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$kotlinxSerializationVersion")
            }
        }
        val jvmTest by getting {
            tasks.withType<Test> {
                useJUnitPlatform()
            }

            dependencies {
                // Implicitly declare to force 1.5.0 version that kotest doesn't use yet
                implementation(kotlin("reflect"))
                implementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
                implementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
                implementation("io.kotest:kotest-property-jvm:$kotestVersion")
                implementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
                implementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
                implementation("org.mock-server:mockserver-netty:5.10")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutinesVersion")
            }
        }
    }
}

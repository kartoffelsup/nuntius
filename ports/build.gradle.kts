plugins {
    kotlin("plugin.serialization") version "1.3.70"
}

dependencies {
    val arrowVersion: String by rootProject.extra
    val kotlinxSerializationVersion: String by rootProject.extra

    api("io.arrow-kt:arrow-core:$arrowVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$kotlinxSerializationVersion")
}

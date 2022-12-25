plugins {
    kotlin("plugin.serialization") version ("1.9.10")
}

dependencies {
    val arrowVersion: String by rootProject.extra
    val kotlinxSerializationVersion: String by rootProject.extra

    api("io.arrow-kt:arrow-core:$arrowVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVersion")
}

plugins {
    kotlin("plugin.serialization") version "1.3.70"
    id("maven-publish")
}

publishing {
    repositories {
        mavenLocal()
    }
    publications {
        create<MavenPublication>("maven") {
            artifactId = "nuntius-api"
            groupId = "io.github.kartoffelsup"
            version = "0.0.1-SNAPSHOT"
            from(components["java"])
        }
    }
}

dependencies {
    val kotlinxSerializationVersion: String by extra

    api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$kotlinxSerializationVersion")
}

plugins {
    kotlin("jvm") version "1.3.71"
}

group = "io.github.kartoffelsup"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

subprojects {
    if (project.name !in listOf("web-app", "api", "client")) {
        apply { plugin("kotlin") }
        repositories {
            mavenLocal()
            mavenCentral()
        }

        tasks {
            compileKotlin {
                kotlinOptions.jvmTarget = "1.8"
            }
            compileTestKotlin {
                kotlinOptions.jvmTarget = "1.8"
            }
        }

        dependencies {
            implementation(kotlin("stdlib-jdk8"))
        }
    }
}

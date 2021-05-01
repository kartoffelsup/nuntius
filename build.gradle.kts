plugins {
    kotlin("jvm") version "1.5.0"
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
                kotlinOptions {
                    jvmTarget = "11"
                }
            }
            compileTestKotlin {
                kotlinOptions {
                    jvmTarget = "11"
                }
            }
        }

        dependencies {
            implementation(kotlin("stdlib-jdk8"))
        }
    }
}

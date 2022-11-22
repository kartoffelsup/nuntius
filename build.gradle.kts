plugins {
    kotlin("jvm") version "1.7.21"
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

        configurations.all {
            resolutionStrategy.eachDependency {
                if (requested.group == "org.slf4j" && requested.name == "slf4j-api") {
                    useVersion("1.7.32")
                    because("slf4j is weird and api >= 2 breaks logging")
                }
            }
        }

        tasks {
            compileKotlin {
                kotlinOptions {
                    jvmTarget = "17"
                }
            }
            compileTestKotlin {
                kotlinOptions {
                    jvmTarget = "17"
                }
            }
        }

        dependencies {
            implementation(kotlin("stdlib-jdk8"))
        }
    }
}

plugins {
    kotlin("jvm") version "1.3.70"
}

group = "io.github.kartoffelsup"
version = "1.0-SNAPSHOT"

allprojects {
    apply {
        plugin("kotlin")
    }

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

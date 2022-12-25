plugins {
    kotlin("multiplatform")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    js {
        browser()
        binaries.executable()
        useCommonJs()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))

                api(project(":api"))
                api(project(":client"))

                val kotlinxSerializationVersion: String by rootProject.extra
                val coroutinesVersion: String by rootProject.extra
                val arrowVersion: String by rootProject.extra

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVersion")
                api("io.arrow-kt:arrow-core:$arrowVersion")

                // KT JS
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.2.0-pre.443")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.2.0-pre.443")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:6.3.0-pre.443")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:11.10.5-pre.443")

                // CSS
                implementation(npm("styled-components", "*"))
                implementation(npm("inline-style-prefixer", "*"))
                implementation(npm("@material/list", "*"))
                implementation(npm("@material/textfield", "*"))
                implementation(npm("@material/button", "*"))
                implementation(npm("material-design-icons", "*"))

                // Webpack
                implementation(npm("mini-css-extract-plugin", "*"))
                implementation(npm("html-webpack-plugin", "*"))
                implementation(npm("style-loader", "*"))
                implementation(npm("css-loader", "*"))

                // REACT
                implementation(npm("react", "18.2.0"))
                implementation(npm("react-dom", "18.2.0"))
                implementation(npm("react-router", "6.3.0"))
                implementation(npm("react-router-dom", "6.3.0"))

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            }
        }
    }
}

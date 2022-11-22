plugins {
    kotlin("js")
}

repositories {
    mavenLocal()
    mavenCentral()
}

// https://play.kotlinlang.org/hands-on/Building%20Web%20Applications%20with%20React%20and%20Kotlin%20JS/02_Setting_up
dependencies {
    implementation(kotlin("stdlib-js"))

    api(project(":api"))
    api(project(":client"))

    val kotlinxSerializationVersion: String by rootProject.extra
    val coroutinesVersion: String by rootProject.extra

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVersion")

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

kotlin {
    js {
        browser()
        useCommonJs()
    }
}

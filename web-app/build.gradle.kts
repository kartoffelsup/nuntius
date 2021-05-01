plugins {
    kotlin("js")
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://kotlin.bintray.com/kotlin-js-wrappers/")
}

// https://play.kotlinlang.org/hands-on/Building%20Web%20Applications%20with%20React%20and%20Kotlin%20JS/02_Setting_up
dependencies {
    implementation(kotlin("stdlib-js"))

    api(project(":api"))
    api(project(":client"))

    val kotlinxSerializationVersion: String by rootProject.extra
    val coroutinesVersion: String by rootProject.extra

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.3")

    // KT JS
    implementation("org.jetbrains:kotlin-react:17.0.1-pre.148-kotlin-1.4.30")
    implementation("org.jetbrains:kotlin-react-dom:17.0.1-pre.148-kotlin-1.4.30")
    implementation("org.jetbrains:kotlin-react-router-dom:5.2.0-pre.148-kotlin-1.4.30")
    implementation("org.jetbrains:kotlin-styled:5.2.1-pre.148-kotlin-1.4.30")

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
    implementation(npm("react", "17.0.1"))
    implementation(npm("react-dom", "17.0.1"))
    implementation(npm("react-router", "5.2.0"))
    implementation(npm("react-router-dom", "5.2.0"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
}

kotlin {
    js {
        browser()
        useCommonJs()
    }
}

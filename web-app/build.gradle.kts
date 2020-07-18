plugins {
    kotlin("js")
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://kotlin.bintray.com/kotlin-js-wrappers/")
    jcenter {
        content {
            includeModule("org.jetbrains.kotlinx", "kotlinx-html-common")
            includeModule("org.jetbrains.kotlinx", "kotlinx-html-js")
        }
    }
}

// https://play.kotlinlang.org/hands-on/Building%20Web%20Applications%20with%20React%20and%20Kotlin%20JS/02_Setting_up
dependencies {
    implementation(kotlin("stdlib-js"))

    api(project(":api"))
    api(project(":client"))

    val kotlinxSerializationVersion: String by rootProject.extra
    val coroutinesVersion: String by rootProject.extra

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:$kotlinxSerializationVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.1")

    // KT JS
    implementation("org.jetbrains:kotlin-react:16.13.1-pre.110-kotlin-1.3.72")
    implementation("org.jetbrains:kotlin-react-dom:16.13.1-pre.110-kotlin-1.3.72")
    implementation("org.jetbrains:kotlin-react-router-dom:5.1.2-pre.110-kotlin-1.3.72")
    implementation("org.jetbrains:kotlin-styled:1.0.0-pre.110-kotlin-1.3.72")

    // CSS
    implementation(npm("styled-components"))
    implementation(npm("inline-style-prefixer"))
    implementation(npm("@material/list"))
    implementation(npm("@material/textfield"))
    implementation(npm("@material/button"))
    implementation(npm("material-design-icons"))

    // Webpack
    implementation(npm("mini-css-extract-plugin"))
    implementation(npm("html-webpack-plugin"))
    implementation(npm("style-loader"))
    implementation(npm("css-loader"))

    // REACT
    implementation(npm("react", "16.13.1"))
    implementation(npm("react-dom", "16.13.1"))
    implementation(npm("react-router", "5.1.2"))
    implementation(npm("react-router-dom", "5.1.2"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$coroutinesVersion")
}

kotlin.target {
    browser()
    useCommonJs()
}

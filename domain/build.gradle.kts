dependencies {
    val arrowVersion: String by rootProject.extra

    api(project(":ports"))
    implementation("io.arrow-kt:arrow-core:$arrowVersion")
}

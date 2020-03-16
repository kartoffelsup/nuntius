dependencies {
    val arrowVersion: String by extra

    api(project(":ports"))
    implementation("io.arrow-kt:arrow-core:$arrowVersion")
}

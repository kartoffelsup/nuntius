dependencies {
    val ktorVersion: String by extra
    val gsonVersion: String by extra
    val postgresVersion: String by extra
    val hikariVersion: String by extra
    val log4jVersion: String by extra

    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")

    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("io.github.kartoffelsup:argparsing")

    implementation(project(":adapters"))
    implementation(project(":domain"))

    runtime("org.apache.logging.log4j:log4j-core:$log4jVersion")
    runtime("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
}

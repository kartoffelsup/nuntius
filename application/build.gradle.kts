dependencies {
    val ktorVersion: String by rootProject.extra
    val postgresVersion: String by rootProject.extra
    val hikariVersion: String by rootProject.extra
    val log4jVersion: String by rootProject.extra
    val guavaVersion: String by rootProject.extra

    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-auto-head-response:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")

    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("io.github.kartoffelsup:argparsing")
    implementation("com.google.guava:guava:$guavaVersion")
    implementation(kotlin("reflect"))

    implementation(project(":adapters"))
    implementation(project(":domain"))

    runtimeOnly("org.apache.logging.log4j:log4j-core:$log4jVersion")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:$log4jVersion")
}

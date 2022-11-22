rootProject.name = "nuntius"
include(
    "application",
    "domain",
    "ports",
    "adapters",
    "api",
    "web-app",
    "client"
)

includeBuild("../cmd-fp-test") {
    dependencySubstitution {
        substitute(module("io.github.kartoffelsup:argparsing"))
            .using(project(":modules:argparsing"))
    }
}

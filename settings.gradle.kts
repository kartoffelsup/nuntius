rootProject.name = "web-msger"
include(
    "application",
    "domain",
    "ports",
    "adapters"
)

includeBuild("../cmd-fp-test") {
    dependencySubstitution {
        substitute(module("io.github.kartoffelsup:argparsing")).with(project(":modules:argparsing"))
    }
}

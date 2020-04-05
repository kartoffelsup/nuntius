package io.github.kartoffelsup.nuntius.client

data class NuntiusHttpRequest(
    val path: String,
    val method: NuntiusHttpMethod,
    val body: String?
) {
    val headers: MutableMap<String, String> = mutableMapOf()

    fun addHeader(name: String, value: String): Unit {
        headers[name] = value
    }
}

enum class NuntiusHttpMethod {
    GET, POST, DELETE, PUT
}
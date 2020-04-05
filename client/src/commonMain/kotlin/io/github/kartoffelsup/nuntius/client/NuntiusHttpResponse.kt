package io.github.kartoffelsup.nuntius.client

data class NuntiusHttpResponse(
    val status: Int,
    val body: String
) {
    val isSuccess = status in 200 until 300
}

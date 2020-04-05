package io.github.kartoffelsup.nuntius.client

expect class NuntiusHttpClient {
    suspend fun request(request: NuntiusHttpRequest): NuntiusHttpResponse
}

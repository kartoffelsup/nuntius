package io.github.kartoffelsup.nuntius.client

import kotlinx.coroutines.await
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import kotlin.browser.window

actual class NuntiusHttpClient {
    actual suspend fun request(request: NuntiusHttpRequest): NuntiusHttpResponse {
        val response: Response =
            window.fetch(request.path, RequestInit(request.method.name, headers = request.headers, body = request.body))
                .await()
        return NuntiusHttpResponse(response.status.toInt(), response.text().await())
    }
}

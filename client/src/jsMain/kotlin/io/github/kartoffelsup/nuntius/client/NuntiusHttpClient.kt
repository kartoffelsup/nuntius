package io.github.kartoffelsup.nuntius.client

import kotlinx.coroutines.await
import org.w3c.fetch.CORS
import org.w3c.fetch.RequestInit
import org.w3c.fetch.RequestMode
import org.w3c.fetch.Response
import kotlin.browser.window
import kotlin.js.json

actual class NuntiusHttpClient {
    actual suspend fun request(request: NuntiusHttpRequest): NuntiusHttpResponse {
        val response: Response = window.fetch(
            request.path,
            RequestInit(
                method = request.method.name,
                headers = json(*request.headers.map { it.key to it.value }.toTypedArray()),
                body = request.body,
                mode = RequestMode.CORS
            )
        ).await()
        return NuntiusHttpResponse(response.status.toInt(), response.text().await())
    }
}

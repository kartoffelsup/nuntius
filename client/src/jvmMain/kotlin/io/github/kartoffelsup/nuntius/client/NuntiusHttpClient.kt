package io.github.kartoffelsup.nuntius.client

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

actual class NuntiusHttpClient {
    private val okHttpClient: OkHttpClient by lazy { OkHttpClient() }
    actual suspend fun request(request: NuntiusHttpRequest): NuntiusHttpResponse {
        val requestBuilder = Request.Builder()
            .method(request.method.name, request.body?.toRequestBody(request.headers["Content-Type"]?.toMediaType()))
            .url(request.path)
        request.headers.forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }
        val response: Response = okHttpClient.newCall(
            requestBuilder
                .build()
        ).execute()

        response.use {
            return NuntiusHttpResponse(it.code, it.body!!.string())
        }
    }
}

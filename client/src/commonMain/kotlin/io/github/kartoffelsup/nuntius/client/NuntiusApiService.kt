package io.github.kartoffelsup.nuntius.client

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

sealed class ApiResult
data class Success<T>(val payload: T) : ApiResult()
data class Failure(val reason: String) : ApiResult()

class NuntiusApiService(private val endpoint: String, private val client: NuntiusHttpClient, private val jsonx: Json) {
    suspend fun <A, B> post(
        path: String,
        request: A,
        requestSerializer: KSerializer<A>,
        responseSerializer: KSerializer<B>,
        credentials: String? = null
    ): ApiResult = request(path, NuntiusHttpMethod.POST, request, requestSerializer, responseSerializer, credentials)

    suspend fun <B> get(
        path: String,
        responseSerializer: KSerializer<B>,
        credentials: String? = null
    ): ApiResult = request(path, NuntiusHttpMethod.GET, null, null, responseSerializer, credentials)

    private suspend fun <A, B> request(
        path: String,
        method: NuntiusHttpMethod,
        request: A? = null,
        requestSerializer: KSerializer<A>? = null,
        responseSerializer: KSerializer<B>,
        credentials: String? = null
    ): ApiResult {
        val authTokenHeader = credentials?.let { creds: String ->
            "Bearer $creds"
        }
        val body: String? = requestSerializer?.let {
            request?.let {
                jsonx.encodeToString(requestSerializer, request)
            }
        }
        val httpRequest = NuntiusHttpRequest(
            path = "$endpoint/api/$path",
            method = method,
            body = body
        )
        body?.let {
            httpRequest.addHeader("Content-Type", "application/json")
        }
        authTokenHeader?.let {
            httpRequest.addHeader("Authorization", it)
        }

        val response: NuntiusHttpResponse = client.request(httpRequest)
        val payload = response.body
        return if (response.isSuccess) {
            Success(jsonx.decodeFromString(responseSerializer, payload))
        } else {
            Failure(payload)
        }
    }
}

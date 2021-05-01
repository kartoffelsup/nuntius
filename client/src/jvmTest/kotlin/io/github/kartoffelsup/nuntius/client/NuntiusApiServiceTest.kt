package io.github.kartoffelsup.nuntius.client

import io.kotest.core.spec.style.StringSpec
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse

internal class NuntiusApiServiceTest : StringSpec({
    "test api" {
        val mock = ClientAndServer.startClientAndServer(9000)
        mock.`when`(HttpRequest.request().withPath("/api/test"))
            .respond(HttpResponse.response().withBody("{\"test\":\"test\"}"))
        val api = NuntiusApiService("http://127.0.0.1:${mock.localPort}", NuntiusHttpClient(), Json {})
        val result = api.post("test", "foo", String.serializer(), JsonObject.serializer())
        println(result)
        mock.stop()
    }
})

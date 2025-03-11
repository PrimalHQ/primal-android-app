package net.primal.networking.http

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import net.primal.PrimalLib

internal fun HttpClientConfig<*>.installSharedHttpClientConfiguration(
    json: Json,
) {
    install(ContentNegotiation) {
        json(json = json)
    }

    defaultRequest {
        contentType(ContentType.Application.Json)
    }

    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                Napier.v(message = message, tag = "HttpClient")
            }
        }
        level = LogLevel.INFO
    }

//    HttpResponseValidator {
//        validateResponse { response ->
//            if (!response.status.isSuccess()) {
//                throw ResponseException(
//                    response = response,
//                    cachedResponseText = "HTTP status is not successful: ${response.status}",
//                )
//            }
//        }
//    }
}

internal fun HttpClientConfig<*>.installWebSocketsHttpClientConfiguration() {
    PrimalLib.userAgent?.let { userAgent ->
        install(UserAgent) {
            agent = userAgent
        }
    }

    install(WebSockets) { }
}

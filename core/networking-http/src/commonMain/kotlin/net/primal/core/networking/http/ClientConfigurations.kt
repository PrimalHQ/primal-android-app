package net.primal.core.networking.http

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import net.primal.core.networking.UserAgentProvider

internal fun HttpClientConfig<*>.installDefaultHttpClientConfiguration(
    json: Json,
    userAgent: String? = UserAgentProvider.resolveUserAgent(),
    loggingTag: String? = "HttpClient",
) {
    install(ContentNegotiation) {
        json(json = json)
    }

    defaultRequest {
        contentType(ContentType.Application.Json)
    }

    if (loggingTag != null) {
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Napier.v(message = message, tag = loggingTag)
                }
            }
            level = LogLevel.ALL
        }
    }

    userAgent?.let {
        install(UserAgent) {
            agent = it
        }
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

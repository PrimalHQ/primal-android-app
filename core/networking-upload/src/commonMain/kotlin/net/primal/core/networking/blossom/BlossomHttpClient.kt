package net.primal.core.networking.blossom

import io.github.aakira.napier.Napier
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.request.HttpSendPipeline
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import net.primal.core.networking.UserAgentProvider
import net.primal.core.networking.factory.HttpClientFactory

internal fun createBlossomHttpClient() =
    HttpClientFactory.createHttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = Long.MAX_VALUE
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 15_000
        }

        install(ContentNegotiation) {
            json(
                json = Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                },
            )
        }

        defaultRequest {
            contentType(ContentType.Application.Json)
        }

        install(UserAgent) {
            agent = UserAgentProvider.resolveUserAgent()
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Napier.v(message = message, tag = "BlossomHttpClient")
                }
            }
            level = LogLevel.ALL
        }
    }.apply {
        // Required workaround to remove `ContentLength: 0`
        // This code executes after the logging (this is why we can see it in logs)
        requestPipeline.intercept(HttpRequestPipeline.Render) {
            if (context.method == HttpMethod.Head) {
                context.headers.remove(HttpHeaders.ContentLength)
            }
            proceed()
        }

        sendPipeline.intercept(HttpSendPipeline.Monitoring) {
            Napier.i(tag = "BlossomHttpClient") {
                "${context.headers.entries()}"
            }
            proceed()
        }
    }

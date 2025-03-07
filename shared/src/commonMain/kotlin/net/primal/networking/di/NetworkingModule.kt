package net.primal.networking.di

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineFactory
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
import net.primal.serialization.json.NostrJson
import org.koin.dsl.module

expect fun createHttpClientEngine(): HttpClientEngineFactory<*>

private fun HttpClientConfig<*>.installSharedConfiguration(
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

internal val networkingModule = module {
    single<HttpClient>(WebSocketHttpClient) {
        HttpClient(createHttpClientEngine()) {
            installSharedConfiguration(json = NostrJson)

            PrimalLib.userAgent?.let { userAgent ->
                install(UserAgent) {
                    agent = userAgent
                }
            }


            install(WebSockets) {

            }
        }
    }

    single<HttpClient>(RegularHttpClient) {
        HttpClient(createHttpClientEngine()) {
            installSharedConfiguration(json = NostrJson)
        }
    }
}

//@Module
//@InstallIn(SingletonComponent::class)
//object NetworkingModule {
//    @Provides
//    @Singleton
//    fun unauthenticatedRetrofit(okHttpClient: OkHttpClient): Retrofit =
//        Retrofit.Builder()
//            .baseUrl("https://primal.net")
//            .client(okHttpClient)
//            .addConverterFactory(ScalarsConverterFactory.create())
//            .addConverterFactory(NostrJson.asConverterFactory("application/json".toMediaType()))
//            .build()
//}

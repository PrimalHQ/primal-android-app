package net.primal.networking.di

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import net.primal.networking.http.createHttpClientEngine
import net.primal.networking.http.installSharedHttpClientConfiguration
import net.primal.networking.http.installWebSocketsHttpClientConfiguration
import net.primal.data.serialization.NostrJson
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.module


object WebSocketHttpClient : Qualifier {
    override val value = "WebSocketHttpClient"
}

object RegularHttpClient : Qualifier {
    override val value = "RegularHttpClient"
}

internal val httpClientModule = module {
    single<HttpClient>(WebSocketHttpClient) {
        HttpClient(createHttpClientEngine()) {
            installSharedHttpClientConfiguration(json = NostrJson)
            installWebSocketsHttpClientConfiguration()
        }
    }

    single<HttpClient>(RegularHttpClient) {
        HttpClient(createHttpClientEngine()) {
            installSharedHttpClientConfiguration(json = NostrJson)
        }
    }

    single<Ktorfit> {
        Ktorfit.Builder()
            .baseUrl("https://primal.net/")
            .httpClient(client = get(RegularHttpClient))
            .build()
    }
}

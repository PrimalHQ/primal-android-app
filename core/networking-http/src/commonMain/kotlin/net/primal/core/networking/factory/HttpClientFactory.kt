package net.primal.core.networking.factory

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import net.primal.core.networking.http.createHttpClientEngine
import net.primal.core.networking.http.installDefaultHttpClientConfiguration
import net.primal.core.networking.serialization.NetworkingJson

object HttpClientFactory {

    private val engine = createHttpClientEngine()

    fun createHttpClient(
        config: HttpClientConfig<*>.() -> Unit,
    ): HttpClient {
        return HttpClient(engine) {
            config()
        }
    }

    fun createHttpClientWithDefaultConfig(
        config: (HttpClientConfig<*>.() -> Unit)? = null,
    ): HttpClient {
        return HttpClient(engine) {
            installDefaultHttpClientConfiguration(json = NetworkingJson)
            config?.invoke(this)
        }
    }

}

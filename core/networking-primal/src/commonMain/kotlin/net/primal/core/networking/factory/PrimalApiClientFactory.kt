package net.primal.core.networking.factory

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import net.primal.core.config.AppConfigFactory
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.ProxyPrimalApiClient
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.coroutines.DispatcherProviderFactory
import net.primal.domain.global.PrimalServerType

internal val defaultSocketsHttpClient by lazy {
    HttpClientFactory.createHttpClientWithDefaultConfig {
        install(WebSockets)
    }
}

object PrimalApiClientFactory {

    private val clients: MutableMap<PrimalServerType, PrimalApiClient> = mutableMapOf()

    fun getDefault(serverType: PrimalServerType): PrimalApiClient {
        return clients.getOrPut(serverType) {
            create(serverType = serverType)
        }
    }

    fun create(serverType: PrimalServerType): PrimalApiClient {
        return create(
            dispatcherProvider = DispatcherProviderFactory.create(),
            httpClient = defaultSocketsHttpClient,
            serverType = serverType,
        )
    }

    fun create(
        dispatcherProvider: DispatcherProvider,
        serverType: PrimalServerType,
        httpClient: HttpClient,
    ): PrimalApiClient {
        return ProxyPrimalApiClient(
            dispatcherProvider = dispatcherProvider,
            httpClient = httpClient,
            serverType = serverType,
            appConfigProvider = AppConfigFactory.createAppConfigProvider(),
            appConfigHandler = AppConfigFactory.createAppConfigHandler(),
        )
    }
}

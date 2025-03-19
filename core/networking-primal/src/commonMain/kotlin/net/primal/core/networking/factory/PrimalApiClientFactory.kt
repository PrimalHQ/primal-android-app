package net.primal.core.networking.factory

import io.ktor.client.HttpClient
import net.primal.core.config.AppConfigFactory
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.ProxyPrimalApiClient
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.PrimalServerType

object PrimalApiClientFactory {

    private val socketsHttpClient by lazy {
        HttpClientFactory.createHttpClientWithDefaultConfig()
    }

    private val clients: MutableMap<PrimalServerType, PrimalApiClient> = mutableMapOf()

    fun getDefault(
        dispatcherProvider: DispatcherProvider,
        serverType: PrimalServerType,
    ): PrimalApiClient {
        return clients.getOrPut(serverType) {
            create(
                dispatcherProvider = dispatcherProvider,
                serverType = serverType,
            )
        }
}

    fun create(
        dispatcherProvider: DispatcherProvider,
        serverType: PrimalServerType,
    ): PrimalApiClient {
        return create(
            dispatcherProvider = dispatcherProvider,
            httpClient = socketsHttpClient,
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
            appConfigProvider = AppConfigFactory.createAppConfigProvider(dispatcherProvider),
            appConfigHandler = AppConfigFactory.createAppConfigHandler(dispatcherProvider),
        )
    }
}

package net.primal.core.networking.factory

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import net.primal.core.config.AppConfigFactory
import net.primal.core.networking.primal.BasePrimalHttpApiClient
import net.primal.core.networking.primal.DEFAULT_HTTP_API_BASE_URL
import net.primal.core.networking.primal.HTTP_API_QUERY_TIMEOUT
import net.primal.core.networking.primal.PrimalHttpApiClient
import net.primal.core.networking.primal.createPrimalHttpApi
import net.primal.domain.global.PrimalServerType

internal val defaultHttpApiClient: HttpClient by lazy {
    HttpClientFactory.createHttpClientWithDefaultConfig {
        install(HttpTimeout) {
            requestTimeoutMillis = HTTP_API_QUERY_TIMEOUT.inWholeMilliseconds
            socketTimeoutMillis = HTTP_API_QUERY_TIMEOUT.inWholeMilliseconds
        }
    }
}

object PrimalHttpApiClientFactory {

    private val clients: MutableMap<PrimalServerType, PrimalHttpApiClient> = mutableMapOf()

    fun getDefault(serverType: PrimalServerType): PrimalHttpApiClient {
        return clients.getOrPut(serverType) {
            create(serverType = serverType)
        }
    }

    fun create(serverType: PrimalServerType): PrimalHttpApiClient {
        return create(serverType = serverType, httpClient = defaultHttpApiClient)
    }

    fun create(serverType: PrimalServerType, httpClient: HttpClient): PrimalHttpApiClient {
        return BasePrimalHttpApiClient(
            serverType = serverType,
            appConfigProvider = AppConfigFactory.createAppConfigProvider(),
            primalHttpApi = Ktorfit.Builder()
                .baseUrl(DEFAULT_HTTP_API_BASE_URL)
                .httpClient(client = httpClient)
                .build()
                .createPrimalHttpApi(),
        )
    }
}

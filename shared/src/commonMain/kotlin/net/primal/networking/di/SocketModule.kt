package net.primal.networking.di

import net.primal.networking.primal.PrimalApiClient
import net.primal.networking.primal.PrimalApiClientImpl
import net.primal.networking.primal.PrimalServerType
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.module

object PrimalCacheApiClient : Qualifier {
    override val value = "PrimalCacheApiClient"
}

object PrimalUploadApiClient : Qualifier {
    override val value = "PrimalUploadApiClient"
}

object PrimalWalletApiClient : Qualifier {
    override val value = "PrimalWalletApiClient"
}

internal val socketsModule = module {

    single<PrimalApiClient>(
        qualifier = PrimalCacheApiClient,
        createdAtStart = true,
    ) {
        PrimalApiClientImpl(
            dispatcherProvider = get(),
            httpClient = get(WebSocketHttpClient),
            serverType = PrimalServerType.Caching,
            appConfigProvider = get(),
            appConfigHandler = get(),
        )
    }

    single<PrimalApiClient>(
        qualifier = PrimalUploadApiClient,
    ) {
        PrimalApiClientImpl(
            dispatcherProvider = get(),
            httpClient = get(WebSocketHttpClient),
            serverType = PrimalServerType.Upload,
            appConfigProvider = get(),
            appConfigHandler = get(),
        )
    }

    single<PrimalApiClient>(
        qualifier = PrimalWalletApiClient,
    ) {
        PrimalApiClientImpl(
            dispatcherProvider = get(),
            httpClient = get(WebSocketHttpClient),
            serverType = PrimalServerType.Wallet,
            appConfigProvider = get(),
            appConfigHandler = get(),
        )
    }
}

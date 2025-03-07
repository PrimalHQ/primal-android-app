package net.primal.networking.di

import net.primal.networking.primal.PrimalApiClient
import net.primal.networking.primal.PrimalServerType
import org.koin.dsl.module

internal val socketsModule = module {

    single(PrimalCacheApiClient) {
        PrimalApiClient(
            dispatcherProvider = get(),
            httpClient = get(WebSocketHttpClient),
            serverType = PrimalServerType.Caching,
//            appConfigProvider = appConfigProvider,
//            appConfigHandler = appConfigHandler,
        )
    }


    single(PrimalUploadApiClient) {
        PrimalApiClient(
            dispatcherProvider = get(),
            httpClient = get(WebSocketHttpClient),
            serverType = PrimalServerType.Upload,
//            appConfigProvider = appConfigProvider,
//            appConfigHandler = appConfigHandler,
        )
    }


    single(PrimalWalletApiClient) {
        PrimalApiClient(
            dispatcherProvider = get(),
            httpClient = get(WebSocketHttpClient),
            serverType = PrimalServerType.Wallet,
//            appConfigProvider = appConfigProvider,
//            appConfigHandler = appConfigHandler,
        )
    }
}

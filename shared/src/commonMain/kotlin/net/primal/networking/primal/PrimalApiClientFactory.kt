package net.primal.networking.primal

import net.primal.PrimalLib
import net.primal.networking.di.PrimalCacheApiClient
import net.primal.networking.di.PrimalUploadApiClient
import net.primal.networking.di.PrimalWalletApiClient

object PrimalApiClientFactory {

    fun getDefault(serverType: PrimalServerType) : PrimalApiClient {
        return PrimalLib.getKoin().get<PrimalApiClient>(
            when (serverType) {
                PrimalServerType.Caching -> PrimalCacheApiClient
                PrimalServerType.Upload -> PrimalUploadApiClient
                PrimalServerType.Wallet -> PrimalWalletApiClient
            }
        )
    }

    fun create(serverType: PrimalServerType) : PrimalApiClient {
        val koin = PrimalLib.getKoin()
        return PrimalApiClientImpl(
            dispatcherProvider = koin.get(),
            httpClient = koin.get(),
            serverType = serverType,
//            appConfigProvider = appConfigProvider,
//            appConfigHandler = appConfigHandler,
        )
    }

}

package net.primal.core.networking.factory

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalServerType

object PrimalApiClientFactory {

    private val clients: MutableMap<PrimalServerType, PrimalApiClient> = mutableMapOf()

    fun initPrimalAliClient(serverType: PrimalServerType): PrimalApiClient {
        if (clients.contains(serverType)) {
            throw IllegalStateException("${serverType}PrimalApiClient already initialized.")
        }

        clients[serverType] = create(serverType)
        throw NotImplementedError()
    }

    fun getDefault(serverType: PrimalServerType): PrimalApiClient {
        throw NotImplementedError()
//        return clients.getOrPut(serverType) {
//            when (serverType) {
//                PrimalServerType.Caching -> PrimalCacheApiClient
//                PrimalServerType.Upload -> PrimalUploadApiClient
//                PrimalServerType.Wallet -> PrimalWalletApiClient
//            }
//        }
    }

    fun createPrimalApiClient(): PrimalApiClient {
        throw NotImplementedError()
//         return PrimalApiClientImpl(
//            dispatcherProvider = get(),
//            httpClient = get(WebSocketHttpClient),
//            serverType = PrimalServerType.Caching,
//            appConfigProvider = get(),
//            appConfigHandler = get(),
//        )
    }

    private fun create(serverType: PrimalServerType): PrimalApiClient {
        throw NotImplementedError()
//        val koin = PrimalLib.getKoin()
//        return PrimalApiClientImpl(
//            dispatcherProvider = koin.get(),
//            httpClient = koin.get(),
//            serverType = serverType,
//            appConfigProvider = koin.get(),
//            appConfigHandler = koin.get(),
//        )
    }
}

//internal val socketsModule = module {
//
//    single<PrimalApiClient>(
//        qualifier = PrimalCacheApiClient,
//        createdAtStart = true,
//    ) {
//        PrimalApiClientImpl(
//            dispatcherProvider = get(),
//            httpClient = get(WebSocketHttpClient),
//            serverType = PrimalServerType.Caching,
//            appConfigProvider = get(),
//            appConfigHandler = get(),
//        )
//    }
//
//    single<PrimalApiClient>(
//        qualifier = PrimalUploadApiClient,
//    ) {
//        PrimalApiClientImpl(
//            dispatcherProvider = get(),
//            httpClient = get(WebSocketHttpClient),
//            serverType = PrimalServerType.Upload,
//            appConfigProvider = get(),
//            appConfigHandler = get(),
//        )
//    }
//
//    single<PrimalApiClient>(
//        qualifier = PrimalWalletApiClient,
//    ) {
//        PrimalApiClientImpl(
//            dispatcherProvider = get(),
//            httpClient = get(WebSocketHttpClient),
//            serverType = PrimalServerType.Wallet,
//            appConfigProvider = get(),
//            appConfigHandler = get(),
//        )
//    }
//}

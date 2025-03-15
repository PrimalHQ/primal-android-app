package net.primal.core.networking.di

//object PrimalCacheApiClient : Qualifier {
//    override val value = "PrimalCacheApiClient"
//}
//
//object PrimalUploadApiClient : Qualifier {
//    override val value = "PrimalUploadApiClient"
//}
//
//object PrimalWalletApiClient : Qualifier {
//    override val value = "PrimalWalletApiClient"
//}
//
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

package net.primal.core.networking.di

//internal val appConfigModule = module {
//
//    single<WellKnownApi> {
//        get<Ktorfit>().createWellKnownApi()
//    }
//
//    single<AppConfigDataStore> {
//        AppConfigDataStore(
//            dispatcherProvider = get(),
//            persistence = get(),
//        )
//    }
//
//    factory<AppConfigProvider> {
//        DynamicAppConfigProvider(
//            appConfigStore = get(),
//            dispatcherProvider = get(),
//        )
//    }
//
//    factory<AppConfigHandler> {
//        AppConfigHandler(
//            dispatcherProvider = get(),
//            appConfigStore = get(),
//            wellKnownApi = get(),
//        )
//    }
//}

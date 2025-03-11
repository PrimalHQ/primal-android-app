package net.primal.networking.config.di

import de.jensklingenberg.ktorfit.Ktorfit
import net.primal.networking.config.AppConfigHandler
import net.primal.networking.config.AppConfigProvider
import net.primal.networking.config.DynamicAppConfigProvider
import net.primal.networking.config.api.WellKnownApi
import net.primal.networking.config.api.createWellKnownApi
import net.primal.networking.config.store.AppConfigDataStore
import org.koin.dsl.module

internal val appConfigModule = module {

    single<WellKnownApi> {
        get<Ktorfit>().createWellKnownApi()
    }

    single<AppConfigDataStore> {
        AppConfigDataStore(
            dispatcherProvider = get(),
            persistence = get(),
        )
    }

    factory<AppConfigProvider> {
        DynamicAppConfigProvider(
            appConfigStore = get(),
            dispatcherProvider = get(),
        )
    }

    factory<AppConfigHandler> {
        AppConfigHandler(
            dispatcherProvider = get(),
            appConfigStore = get(),
            wellKnownApi = get(),
        )
    }
}

package net.primal.networking.primal.api.di

import net.primal.networking.di.PrimalCacheApiClient
import net.primal.networking.primal.api.PrimalImportApi
import net.primal.networking.primal.api.PrimalImportApiImpl
import org.koin.dsl.module

internal val primalImportApiModule = module {
    factory<PrimalImportApi> {
        PrimalImportApiImpl(
            primalApiClient = get(PrimalCacheApiClient),
        )
    }
}

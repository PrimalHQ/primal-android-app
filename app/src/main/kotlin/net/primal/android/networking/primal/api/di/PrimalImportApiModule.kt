package net.primal.android.networking.primal.api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.api.PrimalImportApi
import net.primal.android.networking.primal.api.PrimalImportApiImpl

@Module
@InstallIn(SingletonComponent::class)
object PrimalImportApiModule {
    @Provides
    fun providePrimalImportApi(@PrimalCacheApiClient primalApiClient: PrimalApiClient): PrimalImportApi =
        PrimalImportApiImpl(
            primalApiClient = primalApiClient,
        )
}

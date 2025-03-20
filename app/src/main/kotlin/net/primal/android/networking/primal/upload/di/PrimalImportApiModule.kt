package net.primal.android.networking.primal.upload.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.data.remote.api.importing.PrimalImportApi
import net.primal.data.remote.factory.PrimalApiFactory

@Module
@InstallIn(SingletonComponent::class)
object PrimalImportApiModule {
    @Provides
    fun providePrimalImportApi(@PrimalCacheApiClient primalApiClient: PrimalApiClient): PrimalImportApi =
        PrimalApiFactory.createImportApi(primalApiClient)
}

package net.primal.android.networking.primal.api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.api.PrimalImportApi
import net.primal.android.networking.primal.api.PrimalImportApiImpl
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object PrimalImportApiModule {
    @Provides
    fun providePrimalImportApi(
        @Named("Api") primalApiClient: PrimalApiClient,
    ): PrimalImportApi = PrimalImportApiImpl(
        primalApiClient = primalApiClient,
    )

}

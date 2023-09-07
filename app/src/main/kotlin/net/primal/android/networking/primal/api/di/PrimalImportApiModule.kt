package net.primal.android.networking.primal.api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.primal.PrimalClient
import net.primal.android.networking.primal.api.PrimalImportApi
import net.primal.android.networking.primal.api.PrimalImportApiImpl
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object PrimalImportApiModule {
    @Provides
    fun providePrimalImportApi(
        @Named("Api") primalClient: PrimalClient,
    ): PrimalImportApi = PrimalImportApiImpl(
        primalClient = primalClient,
    )

}

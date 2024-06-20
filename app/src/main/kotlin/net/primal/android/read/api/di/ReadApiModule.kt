package net.primal.android.read.api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.read.api.ReadsApi
import net.primal.android.read.api.ReadsApiImpl

@Module
@InstallIn(SingletonComponent::class)
object ReadApiModule {
    @Provides
    fun provideReadsApi(@PrimalCacheApiClient primalApiClient: PrimalApiClient): ReadsApi =
        ReadsApiImpl(
            primalApiClient = primalApiClient,
        )
}

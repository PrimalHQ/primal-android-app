package net.primal.android.stats.api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.stats.api.EventStatsApi
import net.primal.android.stats.api.EventStatsApiImpl

@Module
@InstallIn(SingletonComponent::class)
object EventStatsApiModule {

    @Provides
    fun provideEventStatsApi(@PrimalCacheApiClient primalApiClient: PrimalApiClient): EventStatsApi =
        EventStatsApiImpl(
            primalApiClient = primalApiClient,
        )
}

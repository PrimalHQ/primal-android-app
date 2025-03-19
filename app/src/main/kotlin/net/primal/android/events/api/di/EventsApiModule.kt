package net.primal.android.events.api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.events.api.EventStatsApi
import net.primal.android.events.api.EventStatsApiImpl
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.core.networking.primal.PrimalApiClient

@Module
@InstallIn(SingletonComponent::class)
object EventsApiModule {

    @Provides
    fun provideEventStatsApi(@PrimalCacheApiClient primalApiClient: PrimalApiClient): EventStatsApi =
        EventStatsApiImpl(
            primalApiClient = primalApiClient,
        )
}

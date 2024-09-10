package net.primal.android.notes.api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.notes.api.FeedApi
import net.primal.android.notes.api.FeedApiImpl

@Module
@InstallIn(SingletonComponent::class)
object FeedApiModule {
    @Provides
    fun provideFeedApi(@PrimalCacheApiClient primalApiClient: PrimalApiClient): FeedApi =
        FeedApiImpl(
            primalApiClient = primalApiClient,
        )
}

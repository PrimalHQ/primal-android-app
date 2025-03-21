package net.primal.android.notes.api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.data.remote.api.feed.FeedApi
import net.primal.data.remote.factory.PrimalApiServiceFactory

@Module
@InstallIn(SingletonComponent::class)
object FeedApiModule {
    @Provides
    fun provideFeedApi(@PrimalCacheApiClient primalApiClient: PrimalApiClient): FeedApi =
        PrimalApiServiceFactory.createFeedApi(primalApiClient)
}

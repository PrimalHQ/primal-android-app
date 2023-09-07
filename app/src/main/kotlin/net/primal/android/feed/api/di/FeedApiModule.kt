package net.primal.android.feed.api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.feed.api.FeedApi
import net.primal.android.feed.api.FeedApiImpl
import net.primal.android.networking.primal.PrimalClient
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object FeedApiModule {
    @Provides
    fun provideFeedApi(
        @Named("Api") primalClient: PrimalClient,
    ): FeedApi = FeedApiImpl(
        primalClient = primalClient,
    )

}

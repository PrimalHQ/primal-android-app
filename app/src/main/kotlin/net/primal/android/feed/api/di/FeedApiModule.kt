package net.primal.android.feed.api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.feed.api.FeedApi
import net.primal.android.feed.api.FeedApiImpl
import net.primal.android.networking.sockets.SocketClient

@Module
@InstallIn(SingletonComponent::class)
object FeedApiModule {
    @Provides
    fun provideFeedApi(
        socketClient: SocketClient,
    ): FeedApi = FeedApiImpl(
        socketClient = socketClient,
    )

}
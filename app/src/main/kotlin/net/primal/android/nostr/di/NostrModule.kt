package net.primal.android.nostr.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.sockets.SocketClient
import net.primal.android.nostr.NostrEventsHandler
import net.primal.android.nostr.primal.PrimalApi
import net.primal.android.nostr.primal.PrimalApiImpl

@Module
@InstallIn(SingletonComponent::class)
object NostrModule {

    @Provides
    fun provideCachingServiceApi(
        socketClient: SocketClient,
        nostrEventsHandler: NostrEventsHandler,
    ): PrimalApi = PrimalApiImpl(
        socketClient = socketClient,
        nostrEventsHandler = nostrEventsHandler,
    )

}
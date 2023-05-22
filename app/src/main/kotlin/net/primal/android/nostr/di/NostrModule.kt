package net.primal.android.nostr.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.db.PrimalDatabase
import net.primal.android.networking.sockets.SocketClient
import net.primal.android.nostr.primal.PrimalApi
import net.primal.android.nostr.primal.PrimalApiImpl

@Module
@InstallIn(SingletonComponent::class)
object NostrModule {

    @Provides
    fun providePrimalApi(
        socketClient: SocketClient,
        primalDatabase: PrimalDatabase,
    ): PrimalApi = PrimalApiImpl(
        socketClient = socketClient,
        database = primalDatabase,
    )

}
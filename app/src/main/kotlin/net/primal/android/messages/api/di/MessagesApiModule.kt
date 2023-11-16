package net.primal.android.messages.api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.messages.api.MessagesApi
import net.primal.android.messages.api.MessagesApiImpl
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.nostr.notary.NostrNotary

@Module
@InstallIn(SingletonComponent::class)
object MessagesApiModule {

    @Provides
    fun provideMessagesApi(
        @PrimalCacheApiClient primalApiClient: PrimalApiClient,
        nostrNotary: NostrNotary,
    ): MessagesApi =
        MessagesApiImpl(
            primalApiClient = primalApiClient,
            nostrNotary = nostrNotary,
        )
}

package net.primal.android.feeds.api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.feeds.api.FeedsApi
import net.primal.android.feeds.api.FeedsApiImpl
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.nostr.notary.NostrNotary
import net.primal.networking.primal.PrimalApiClient

@Module
@InstallIn(SingletonComponent::class)
class FeedsApiModule {

    @Provides
    fun provideFeedsMarketplaceApi(
        @PrimalCacheApiClient primalApiClient: PrimalApiClient,
        notary: NostrNotary,
    ): FeedsApi =
        FeedsApiImpl(
            primalApiClient = primalApiClient,
            nostrNotary = notary,
        )
}

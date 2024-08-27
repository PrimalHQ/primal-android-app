package net.primal.android.feeds.api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.feeds.api.FeedsMarketplaceApi
import net.primal.android.feeds.api.FeedsMarketplaceApiImpl
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.primal.PrimalApiClient

@Module
@InstallIn(SingletonComponent::class)
class FeedsMarketplaceApiModule {
    @Provides
    fun provideFeedsMarketplaceApi(@PrimalCacheApiClient primalApiClient: PrimalApiClient): FeedsMarketplaceApi =
        FeedsMarketplaceApiImpl(primalApiClient = primalApiClient)
}

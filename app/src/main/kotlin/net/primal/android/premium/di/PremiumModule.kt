package net.primal.android.premium.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.di.PrimalCacheApiClient
import net.primal.android.networking.di.PrimalWalletApiClient
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.premium.api.PremiumApi
import net.primal.android.premium.api.PremiumApiImpl
import net.primal.core.networking.primal.PrimalApiClient

@Module
@InstallIn(SingletonComponent::class)
object PremiumModule {

    @Provides
    fun providePremiumApi(
        @PrimalWalletApiClient primalApiClient: PrimalApiClient,
        @PrimalCacheApiClient primalCacheApiClient: PrimalApiClient,
        nostrNotary: NostrNotary,
    ): PremiumApi =
        PremiumApiImpl(
            primalWalletApiClient = primalApiClient,
            primalCacheApiClient = primalCacheApiClient,
            nostrNotary = nostrNotary,
        )
}

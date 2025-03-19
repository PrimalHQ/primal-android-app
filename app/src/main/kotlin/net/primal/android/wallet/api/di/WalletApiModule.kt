package net.primal.android.wallet.api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.di.PrimalWalletApiClient
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.wallet.api.NwcPrimalWalletApi
import net.primal.android.wallet.api.NwcPrimalWalletApiImpl
import net.primal.android.wallet.api.WalletApi
import net.primal.android.wallet.api.WalletApiImpl
import net.primal.core.networking.primal.PrimalApiClient

@Module
@InstallIn(SingletonComponent::class)
object WalletApiModule {

    @Provides
    fun provideWalletApi(
        @PrimalWalletApiClient primalApiClient: PrimalApiClient,
        nostrNotary: NostrNotary,
    ): WalletApi =
        WalletApiImpl(
            primalApiClient = primalApiClient,
            nostrNotary = nostrNotary,
        )

    @Provides
    fun provideNwcPrimalWalletApi(
        @PrimalWalletApiClient primalApiClient: PrimalApiClient,
        nostrNotary: NostrNotary,
    ): NwcPrimalWalletApi =
        NwcPrimalWalletApiImpl(
            primalApiClient = primalApiClient,
            nostrNotary = nostrNotary,
        )
}

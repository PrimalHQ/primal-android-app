package net.primal.android.wallet.api.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.di.PrimalWalletApiClient
import net.primal.android.nostr.notary.NostrNotary
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.wallet.data.remote.api.PrimalWalletApi
import net.primal.wallet.data.remote.api.PrimalWalletApiImpl
import net.primal.wallet.data.remote.api.PrimalWalletNwcApi
import net.primal.wallet.data.remote.api.PrimalWalletNwcApiImpl

@Module
@InstallIn(SingletonComponent::class)
object WalletApiModule {

    @Provides
    fun provideWalletApi(
        @PrimalWalletApiClient primalApiClient: PrimalApiClient,
        nostrNotary: NostrNotary,
    ): PrimalWalletApi =
        PrimalWalletApiImpl(
            primalApiClient = primalApiClient,
            signatureHandler = nostrNotary,
        )

    @Provides
    fun provideNwcPrimalWalletApi(
        @PrimalWalletApiClient primalApiClient: PrimalApiClient,
        nostrNotary: NostrNotary,
    ): PrimalWalletNwcApi =
        PrimalWalletNwcApiImpl(
            primalApiClient = primalApiClient,
            signatureHandler = nostrNotary,
        )
}

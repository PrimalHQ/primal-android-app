package net.primal.android.premium.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.di.PrimalWalletApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.premium.api.PremiumApi
import net.primal.android.premium.api.PremiumApiImpl

@Module
@InstallIn(SingletonComponent::class)
object PremiumModule {

    @Provides
    fun providePremiumApi(@PrimalWalletApiClient primalApiClient: PrimalApiClient): PremiumApi =
        PremiumApiImpl(primalApiClient = primalApiClient)
}

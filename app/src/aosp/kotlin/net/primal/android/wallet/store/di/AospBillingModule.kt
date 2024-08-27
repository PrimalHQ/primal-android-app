package net.primal.android.wallet.store.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.primal.android.wallet.store.AospBillingClient
import net.primal.android.wallet.store.PrimalBillingClient

@Module
@InstallIn(SingletonComponent::class)
object AospBillingModule {

    @Provides
    @Singleton
    fun providePlayBillingClient(): PrimalBillingClient = AospBillingClient()
}

package net.primal.android.networking.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.primal.networking.primal.PrimalApiClientFactory
import net.primal.networking.primal.PrimalServerType
import net.primal.networking.sockets.NostrSocketClientFactory

@Module
@InstallIn(SingletonComponent::class)
object SocketModule {

    @Provides
    @Singleton
    @PrimalCacheApiClient
    fun providesPrimalApiClient() = PrimalApiClientFactory.getDefault(PrimalServerType.Caching)

    @Provides
    @Singleton
    @PrimalUploadApiClient
    fun providesPrimalUploadClient() = PrimalApiClientFactory.getDefault(PrimalServerType.Upload)

    @Provides
    @Singleton
    @PrimalWalletApiClient
    fun providesPrimalWalletClient() = PrimalApiClientFactory.getDefault(PrimalServerType.Wallet)

    @Singleton
    @Provides
    fun providesNostrSocketClientFactory() = NostrSocketClientFactory
}

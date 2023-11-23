package net.primal.android.networking.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.primal.android.config.AppConfigProvider
import net.primal.android.config.dynamic.AppConfigUpdater
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalServerType
import net.primal.android.networking.relays.RelayPoolFactory
import net.primal.android.networking.relays.RelaysManager
import net.primal.android.user.accounts.active.ActiveAccountStore
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object SocketModule {

    @Provides
    @Singleton
    @PrimalCacheApiClient
    fun providesPrimalApiClient(
        okHttpClient: OkHttpClient,
        appConfigProvider: AppConfigProvider,
        appConfigUpdater: AppConfigUpdater,
    ) =
        PrimalApiClient(
            okHttpClient = okHttpClient,
            serverType = PrimalServerType.Caching,
            appConfigProvider = appConfigProvider,
            appConfigUpdater = appConfigUpdater,
        )

    @Provides
    @Singleton
    @PrimalUploadApiClient
    fun providesPrimalUploadClient(
        okHttpClient: OkHttpClient,
        appConfigProvider: AppConfigProvider,
        appConfigUpdater: AppConfigUpdater,
    ) =
        PrimalApiClient(
            okHttpClient = okHttpClient,
            serverType = PrimalServerType.Upload,
            appConfigProvider = appConfigProvider,
            appConfigUpdater = appConfigUpdater,
        )

    @Provides
    @Singleton
    fun providesRelaysManager(relayPoolFactory: RelayPoolFactory, activeAccountStore: ActiveAccountStore) =
        RelaysManager(
            relayPoolFactory = relayPoolFactory,
            activeAccountStore = activeAccountStore,
        )
}

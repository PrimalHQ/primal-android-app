package net.primal.android.networking.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.primal.android.config.AppConfigHandler
import net.primal.android.config.AppConfigProvider
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalServerType
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object SocketModule {

    @Provides
    @Singleton
    @PrimalCacheApiClient
    fun providesPrimalApiClient(
        dispatchers: CoroutineDispatcherProvider,
        okHttpClient: OkHttpClient,
        appConfigProvider: AppConfigProvider,
        appConfigHandler: AppConfigHandler,
    ) = PrimalApiClient(
        okHttpClient = okHttpClient,
        serverType = PrimalServerType.Caching,
        appConfigProvider = appConfigProvider,
        appConfigHandler = appConfigHandler,
        dispatcherProvider = dispatchers,
    )

    @Provides
    @Singleton
    @PrimalUploadApiClient
    fun providesPrimalUploadClient(
        dispatchers: CoroutineDispatcherProvider,
        okHttpClient: OkHttpClient,
        appConfigProvider: AppConfigProvider,
        appConfigHandler: AppConfigHandler,
    ) = PrimalApiClient(
        okHttpClient = okHttpClient,
        serverType = PrimalServerType.Upload,
        appConfigProvider = appConfigProvider,
        appConfigHandler = appConfigHandler,
        dispatcherProvider = dispatchers,
    )

    @Provides
    @Singleton
    @PrimalWalletApiClient
    fun providesPrimalWalletClient(
        dispatchers: CoroutineDispatcherProvider,
        okHttpClient: OkHttpClient,
        appConfigProvider: AppConfigProvider,
        appConfigHandler: AppConfigHandler,
    ) = PrimalApiClient(
        okHttpClient = okHttpClient,
        serverType = PrimalServerType.Wallet,
        appConfigProvider = appConfigProvider,
        appConfigHandler = appConfigHandler,
        dispatcherProvider = dispatchers,
    )
}

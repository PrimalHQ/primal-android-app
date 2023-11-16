package net.primal.android.networking.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.primal.android.networking.UserAgentProvider
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.relays.RelayPoolFactory
import net.primal.android.networking.relays.RelaysManager
import net.primal.android.networking.sockets.NostrSocketClient
import net.primal.android.user.accounts.active.ActiveAccountStore
import okhttp3.OkHttpClient
import okhttp3.Request

@Module
@InstallIn(SingletonComponent::class)
object SocketModule {

    @Provides
    @PrimalCacheApiBaseUrl
    fun providePrimalApiWSRequest() =
        Request.Builder()
            .url("wss://cache1.primal.net/v1")
            .addHeader("User-Agent", UserAgentProvider.USER_AGENT)
            .build()

    @Provides
    @PrimalUploadApiBaseUrl
    fun providePrimalUploadWSRequest() =
        Request.Builder()
            .url("wss://uploads.primal.net/v1")
            .addHeader("User-Agent", UserAgentProvider.USER_AGENT)
            .build()

    @Provides
    @Singleton
    @PrimalCacheApiClient
    fun providesPrimalApiClient(okHttpClient: OkHttpClient, @PrimalCacheApiBaseUrl wssRequest: Request) =
        PrimalApiClient(
            socketClient = NostrSocketClient(
                okHttpClient = okHttpClient,
                wssRequest = wssRequest,
            ),
        )

    @Provides
    @Singleton
    @PrimalUploadApiClient
    fun providesPrimalUploadClient(okHttpClient: OkHttpClient, @PrimalUploadApiBaseUrl wssRequest: Request) =
        PrimalApiClient(
            socketClient = NostrSocketClient(
                okHttpClient = okHttpClient,
                wssRequest = wssRequest,
            ),
        )

    @Provides
    @Singleton
    fun providesRelaysManager(relayPoolFactory: RelayPoolFactory, activeAccountStore: ActiveAccountStore) =
        RelaysManager(
            relayPoolFactory = relayPoolFactory,
            activeAccountStore = activeAccountStore,
        )
}

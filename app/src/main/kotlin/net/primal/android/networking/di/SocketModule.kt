package net.primal.android.networking.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.UserAgentProvider
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.relays.RelayPool
import net.primal.android.networking.sockets.NostrSocketClient
import net.primal.android.user.accounts.active.ActiveAccountStore
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SocketModule {

    @Provides
    @PrimalApiBaseUrl
    fun providePrimalApiWSRequest() = Request.Builder()
        .url("wss://cache1.primal.net/v1")
        .addHeader("User-Agent", UserAgentProvider.USER_AGENT)
        .build()

    @Provides
    @PrimalUploadBaseUrl
    fun providePrimalUploadWSRequest() = Request.Builder()
        .url("wss://uploads.primal.net/v1")
        .addHeader("User-Agent", UserAgentProvider.USER_AGENT)
        .build()

    @Provides
    @PrimalSocketClient
    fun providePrimalSocketClient(
        okHttpClient: OkHttpClient,
        @PrimalApiBaseUrl wssRequest: Request,
    ) = NostrSocketClient(
        okHttpClient = okHttpClient,
        wssRequest = wssRequest,
    )

    @Provides
    @PrimalUploadSocketClient
    fun providePrimalUploadSocketClient(
        okHttpClient: OkHttpClient,
        @PrimalUploadBaseUrl wssRequest: Request
    ) = NostrSocketClient(
        okHttpClient = okHttpClient,
        wssRequest = wssRequest
    )

    @Provides
    @Singleton
    fun providesRelayPool(
        okHttpClient: OkHttpClient,
        activeAccountStore: ActiveAccountStore,
    ) = RelayPool(
        okHttpClient = okHttpClient,
        activeAccountStore = activeAccountStore,
    )

    @Provides
    @Singleton
    @Named("Api")
    fun providesPrimalApiClient(
        @PrimalSocketClient socketClient: NostrSocketClient
    ) = PrimalApiClient(
        socketClient = socketClient
    )

    @Provides
    @Singleton
    @Named("Upload")
    fun providesPrimalUploadClient(
        @PrimalUploadSocketClient socketClient: NostrSocketClient
    ) = PrimalApiClient(
        socketClient = socketClient
    )
}

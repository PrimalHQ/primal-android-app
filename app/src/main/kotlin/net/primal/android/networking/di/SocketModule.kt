package net.primal.android.networking.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.UserAgentProvider
import net.primal.android.networking.relays.RelayPool
import net.primal.android.networking.sockets.NostrSocketClient
import net.primal.android.user.accounts.active.ActiveAccountStore
import okhttp3.OkHttpClient
import okhttp3.Request
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
    @PrimalSocketClient
    fun providePrimalSocketClient(
        okHttpClient: OkHttpClient,
        @PrimalApiBaseUrl wssRequest: Request,
    ) = NostrSocketClient(
        okHttpClient = okHttpClient,
        wssRequest = wssRequest,
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
}

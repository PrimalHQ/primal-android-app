package net.primal.android.networking.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.networking.UserAgentProvider
import okhttp3.Request

@Module
@InstallIn(SingletonComponent::class)
object SocketModule {

    @Provides
    @PrimalApiWS
    fun providePrimalApiWSRequest() = Request.Builder()
        .url("wss://cache0.primal.net/cache17")
        .addHeader("User-Agent", UserAgentProvider.USER_AGENT)
        .build()

}

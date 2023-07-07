package net.primal.android.networking.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Request

@Module
@InstallIn(SingletonComponent::class)
object SocketModule {

    @Provides
    @PrimalApiWS
    fun providePrimalApiWSRequest() = Request.Builder()
        .url("wss://cache1.primal.net/v1")
        .build()

}

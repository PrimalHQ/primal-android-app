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
    @CachingService
    fun provideSocketRequest() = Request.Builder()
        .url("wss://cache3.primal.net/cache15")
        .build()

}

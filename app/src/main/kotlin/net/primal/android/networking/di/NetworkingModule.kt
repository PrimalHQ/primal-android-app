package net.primal.android.networking.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkingModule {

    @Provides
    @ElementsIntoSet
    fun emptyInterceptorsSet(): Set<Interceptor> = emptySet()

    private fun OkHttpClient.Builder.withInterceptors(interceptors: Collection<Interceptor>) =
        apply {
            interceptors.forEach { addInterceptor(it) }
        }

    @Provides
    @Singleton
    fun unauthenticatedOkHttpClient(interceptors: Set<@JvmSuppressWildcards Interceptor>) =
        OkHttpClient.Builder()
            .withInterceptors(interceptors)
            .build()
}

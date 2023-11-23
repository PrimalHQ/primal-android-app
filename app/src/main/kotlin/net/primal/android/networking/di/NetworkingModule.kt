package net.primal.android.networking.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import javax.inject.Singleton
import net.primal.android.core.serialization.json.NostrJson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

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

    @Provides
    @Singleton
    fun unauthenticatedRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://primal.net")
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(NostrJson.asConverterFactory("application/json".toMediaType()))
            .build()
}

package net.primal.networking.di

import io.ktor.client.HttpClient
import net.primal.networking.http.createHttpClientEngine
import net.primal.networking.http.installSharedHttpClientConfiguration
import net.primal.networking.http.installWebSocketsHttpClientConfiguration
import net.primal.serialization.json.NostrJson
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.module


object WebSocketHttpClient : Qualifier {
    override val value = "WebSocketHttpClient"
}

object RegularHttpClient : Qualifier {
    override val value = "RegularHttpClient"
}

internal val httpClientModule = module {
    single<HttpClient>(WebSocketHttpClient) {
        HttpClient(createHttpClientEngine()) {
            installSharedHttpClientConfiguration(json = NostrJson)
            installWebSocketsHttpClientConfiguration()
        }
    }

    single<HttpClient>(RegularHttpClient) {
        HttpClient(createHttpClientEngine()) {
            installSharedHttpClientConfiguration(json = NostrJson)
        }
    }
}

//@Module
//@InstallIn(SingletonComponent::class)
//object NetworkingModule {
//    @Provides
//    @Singleton
//    fun unauthenticatedRetrofit(okHttpClient: OkHttpClient): Retrofit =
//        Retrofit.Builder()
//            .baseUrl("https://primal.net")
//            .client(okHttpClient)
//            .addConverterFactory(ScalarsConverterFactory.create())
//            .addConverterFactory(NostrJson.asConverterFactory("application/json".toMediaType()))
//            .build()
//}

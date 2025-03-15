package net.primal.core.networking.factory

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import net.primal.core.networking.http.createHttpClientEngine

object HttpClientFactory {

    private var defaultWebSocketsClient: HttpClient? = null

    internal fun initDefaultWebSocketsClient() {

    }

//    by lazy {
//        createHttpClient {
//            installSharedHttpClientConfiguration(NetworkingJson)
//            installWebSocketsHttpClientConfiguration()
//        }
//    }

    private var defaultHttpClient: HttpClient? = null

    internal fun initDefaultHttpClient() {

    }

//    by lazy {
//        createHttpClient {
//            installSharedHttpClientConfiguration(NetworkingJson)
//        }
//    }



    fun getDefaultHttpClient() = defaultHttpClient

    fun createHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient {
        return HttpClient(createHttpClientEngine()) {
            config()
        }
    }

}

//ebSocketHttpClient : Qualifier {
//    override val value = "WebSocketHttpClient"
//}
//
//object RegularHttpClient : Qualifier {
//    override val value = "RegularHttpClient"
//}
//
//internal val httpClientModule = module {
//    single<HttpClient>(WebSocketHttpClient) {
//        HttpClient(createHttpClientEngine()) {
//            installSharedHttpClientConfiguration(json = NostrJson)
//            installWebSocketsHttpClientConfiguration()
//        }
//    }
//
//    single<HttpClient>(RegularHttpClient) {
//        HttpClient(createHttpClientEngine()) {
//            installSharedHttpClientConfiguration(json = NostrJson)
//        }
//    }
//
//    single<Ktorfit> {
//        Ktorfit.Builder()
//            .baseUrl("https://primal.net/")
//            .httpClient(client = get(RegularHttpClient))
//            .build()
//    }
//}

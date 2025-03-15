package net.primal.core.networking.di


//object WebSocketHttpClient : Qualifier {
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

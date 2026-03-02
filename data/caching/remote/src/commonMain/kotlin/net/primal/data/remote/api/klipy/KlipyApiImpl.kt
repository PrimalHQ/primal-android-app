package net.primal.data.remote.api.klipy

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import net.primal.core.networking.factory.HttpClientFactory
import net.primal.data.remote.api.klipy.model.KlipySearchResponse

internal class KlipyApiImpl(
    apiKey: String,
    clientKey: String,
    private val httpClient: HttpClient,
) : KlipyApi {

    private val remoteApi: KlipyRemoteApi = run {
        val klipyQueryParams = createClientPlugin("KlipyQueryParams") {
            on(Send) { request ->
                request.url.parameters.append("key", apiKey)
                request.url.parameters.append("client_key", clientKey)
                request.url.parameters.append("media_filter", MEDIA_FILTER)
                request.url.parameters.append("contentfilter", CONTENT_FILTER)
                proceed(request)
            }
        }
        val klipyClient = HttpClientFactory.createHttpClientWithDefaultConfig {
            install(klipyQueryParams)
        }
        Ktorfit.Builder()
            .baseUrl(BASE_URL)
            .httpClient(klipyClient)
            .build()
            .createKlipyRemoteApi()
    }

    override suspend fun fetchTrendingGifs(limit: Int, cursor: String?): KlipySearchResponse =
        remoteApi.fetchTrendingGifs(limit = limit, cursor = cursor)

    override suspend fun searchGifs(
        query: String,
        limit: Int,
        cursor: String?,
    ): KlipySearchResponse = remoteApi.searchGifs(query = query, limit = limit, cursor = cursor)

    override suspend fun registerShare(gifId: String, query: String) =
        remoteApi.registerShare(gifId = gifId, query = query)

    override suspend fun downloadGifBytes(url: String): ByteArray = httpClient.get(url).readRawBytes()

    companion object {
        private const val BASE_URL = "https://api.klipy.com/v2/"
        private const val MEDIA_FILTER = "gif,tinygif"
        private const val CONTENT_FILTER = "medium"
    }
}

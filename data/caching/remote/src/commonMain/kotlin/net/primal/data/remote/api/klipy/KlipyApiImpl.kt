package net.primal.data.remote.api.klipy

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.readRawBytes
import net.primal.data.remote.api.klipy.model.KlipySearchResponse

internal class KlipyApiImpl(
    private val apiKey: String,
    private val httpClient: HttpClient,
) : KlipyApi {

    companion object {
        private const val BASE_URL = "https://api.klipy.com/v2"
        private const val CLIENT_KEY = "FIGLamirp"
        private const val MEDIA_FILTER = "gif,tinygif"
        private const val CONTENT_FILTER = "medium"
    }

    override suspend fun fetchTrendingGifs(limit: Int, cursor: String?): KlipySearchResponse {
        return httpClient.get("$BASE_URL/featured") {
            parameter("key", apiKey)
            parameter("client_key", CLIENT_KEY)
            parameter("media_filter", MEDIA_FILTER)
            parameter("contentfilter", CONTENT_FILTER)
            parameter("limit", limit)
            if (cursor != null) parameter("pos", cursor)
        }.body()
    }

    override suspend fun searchGifs(
        query: String,
        limit: Int,
        cursor: String?,
    ): KlipySearchResponse {
        return httpClient.get("$BASE_URL/search") {
            parameter("key", apiKey)
            parameter("client_key", CLIENT_KEY)
            parameter("q", query)
            parameter("media_filter", MEDIA_FILTER)
            parameter("contentfilter", CONTENT_FILTER)
            parameter("limit", limit)
            if (cursor != null) parameter("pos", cursor)
        }.body()
    }

    override suspend fun registerShare(gifId: String, query: String) {
        httpClient.post("$BASE_URL/registershare") {
            parameter("key", apiKey)
            parameter("client_key", CLIENT_KEY)
            parameter("id", gifId)
            if (query.isNotBlank()) parameter("q", query)
        }
    }

    override suspend fun downloadGifBytes(url: String): ByteArray {
        return httpClient.get(url).readRawBytes()
    }
}

package net.primal.data.remote.api.klipy

import net.primal.data.remote.api.klipy.model.KlipySearchResponse

interface KlipyApi {

    suspend fun fetchTrendingGifs(limit: Int = 20, cursor: String? = null): KlipySearchResponse

    suspend fun searchGifs(
        query: String,
        limit: Int = 20,
        cursor: String? = null,
    ): KlipySearchResponse

    suspend fun registerShare(gifId: String, query: String)

    suspend fun downloadGifBytes(url: String): ByteArray
}

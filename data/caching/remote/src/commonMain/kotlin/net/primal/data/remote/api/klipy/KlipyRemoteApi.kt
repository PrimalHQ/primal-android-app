package net.primal.data.remote.api.klipy

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query
import net.primal.data.remote.api.klipy.model.KlipySearchResponse

interface KlipyRemoteApi {

    @GET("featured")
    suspend fun fetchTrendingGifs(@Query("limit") limit: Int, @Query("pos") cursor: String?): KlipySearchResponse

    @GET("search")
    suspend fun searchGifs(
        @Query("q") query: String,
        @Query("limit") limit: Int,
        @Query("pos") cursor: String?,
    ): KlipySearchResponse

    @POST("registershare")
    suspend fun registerShare(@Query("id") gifId: String, @Query("q") query: String)
}

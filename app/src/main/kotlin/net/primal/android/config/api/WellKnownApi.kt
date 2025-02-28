package net.primal.android.config.api

import retrofit2.http.GET
import retrofit2.http.Query

interface WellKnownApi {
    @GET("https://primal.net/.well-known/primal-endpoints.json")
    suspend fun fetchApiConfig(): ApiConfigResponse

    @GET("https://primal.net/.well-known/nostr.json")
    suspend fun fetchProfileId(@Query("name") primalName: String): WellKnownProfileResponse
}

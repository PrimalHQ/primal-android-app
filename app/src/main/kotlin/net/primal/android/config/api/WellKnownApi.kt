package net.primal.android.config.api

import retrofit2.http.GET

interface WellKnownApi {
    @GET("https://primal.net/.well-known/primal-endpoints.json")
    suspend fun fetchApiConfig(): ApiConfigResponse
}

package net.primal.core.networking.config.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query

interface WellKnownApi {

    @GET("https://primal.net/.well-known/primal-endpoints.json")
    suspend fun fetchApiConfig(): ApiConfigResponse

    @GET("https://primal.net/.well-known/nostr.json")
    suspend fun fetchProfileId(@Query("name") primalName: String): WellKnownProfileResponse
}

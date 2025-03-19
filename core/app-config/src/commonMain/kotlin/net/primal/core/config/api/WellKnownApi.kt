package net.primal.core.config.api

import de.jensklingenberg.ktorfit.http.GET

internal interface WellKnownApi {
    @GET("https://primal.net/.well-known/primal-endpoints.json")
    suspend fun fetchApiConfig(): ApiConfigResponse
}

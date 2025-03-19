package net.primal.android.profile.api

import retrofit2.http.GET
import retrofit2.http.Query

interface ProfileWellKnownApi {
    @GET("https://primal.net/.well-known/nostr.json")
    suspend fun fetchProfileId(@Query("name") primalName: String): ProfileWellKnownProfileResponse
}

package net.primal.data.remote.api.users

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import net.primal.data.remote.api.users.model.ProfileWellKnownResponse

interface UserWellKnownApi {
    @GET("https://primal.net/.well-known/nostr.json")
    suspend fun fetchProfileId(@Query("name") primalName: String): ProfileWellKnownResponse
}

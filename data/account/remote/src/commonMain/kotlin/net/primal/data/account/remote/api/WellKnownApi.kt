package net.primal.data.account.remote.api

import de.jensklingenberg.ktorfit.http.GET
import net.primal.data.account.remote.api.model.MediumTrustPermissionsResponse

interface WellKnownApi {
    @GET("https://primal.net/.well-known/remote-signer-nip46-defaults.json")
    suspend fun getMediumTrustPermissions(): MediumTrustPermissionsResponse
}

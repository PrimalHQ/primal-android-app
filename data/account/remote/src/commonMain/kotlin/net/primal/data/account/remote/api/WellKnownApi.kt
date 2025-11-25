package net.primal.data.account.remote.api

import de.jensklingenberg.ktorfit.http.GET
import net.primal.data.account.remote.api.model.MediumTrustPermissionsResponse
import net.primal.data.account.remote.api.model.PermissionsResponse

interface WellKnownApi {
    @GET("https://primal.net/.well-known/remote-signer-nip46-defaults.json")
    suspend fun getMediumTrustPermissions(): MediumTrustPermissionsResponse

    @GET("https://primal.net/.well-known/remote-signer-nip46-permissions.json")
    suspend fun getSignerPermissions(): PermissionsResponse
}

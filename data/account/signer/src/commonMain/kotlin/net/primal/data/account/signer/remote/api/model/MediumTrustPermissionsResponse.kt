package net.primal.data.account.signer.remote.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MediumTrustPermissionsResponse(
    @SerialName("medium_permissions_allow") val allowPermissions: List<String>,
)

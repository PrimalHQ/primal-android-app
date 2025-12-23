package net.primal.domain.account.model

import kotlinx.serialization.Serializable

@Serializable
data class AppPermission(
    val permissionId: String,
    val clientPubKey: String,
    val action: PermissionAction,
)

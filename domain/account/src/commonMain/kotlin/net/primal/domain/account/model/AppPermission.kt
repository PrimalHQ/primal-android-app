package net.primal.domain.account.model

data class AppPermission(
    val permissionId: String,
    val clientPubKey: String,
    val action: PermissionAction,
)

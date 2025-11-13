package net.primal.domain.account.model

data class AppPermission(
    val permissionId: String,
    val connectionId: String,
    val action: PermissionAction,
)

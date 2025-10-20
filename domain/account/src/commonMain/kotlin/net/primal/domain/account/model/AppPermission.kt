package net.primal.domain.account.model

data class AppPermission(
    val permissionId: String,
    val permissionName: String,
    val defaultAction: PermissionAction,
)

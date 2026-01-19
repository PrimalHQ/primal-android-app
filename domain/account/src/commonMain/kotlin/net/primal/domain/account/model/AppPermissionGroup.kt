package net.primal.domain.account.model

data class AppPermissionGroup(
    val groupId: String,
    val title: String,
    val action: AppPermissionAction,
    val permissionIds: List<String>,
)

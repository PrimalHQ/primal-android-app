package net.primal.android.settings.connected.model

import net.primal.domain.account.model.AppPermissionAction
import net.primal.domain.account.model.AppPermissionGroup

data class PermissionGroupUi(
    val groupId: String,
    val title: String,
    val action: AppPermissionAction,
    val permissionIds: List<String>,
)

fun AppPermissionGroup.asPermissionGroupUi() =
    PermissionGroupUi(
        groupId = this.groupId,
        title = this.title,
        action = this.action,
        permissionIds = this.permissionIds,
    )

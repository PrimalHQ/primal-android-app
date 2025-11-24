package net.primal.android.settings.connected.model

import net.primal.domain.account.model.PermissionAction

data class PermissionUi(
    val permissionId: String,
    val title: String,
    val action: PermissionAction,
)

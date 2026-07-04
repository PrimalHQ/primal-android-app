package net.primal.data.account.local.dao.apps

import androidx.room3.Entity

@Entity(primaryKeys = ["permissionId", "appIdentifier"])
data class AppPermissionData(
    val permissionId: String,
    val appIdentifier: String,
    val action: PermissionAction,
)

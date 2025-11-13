package net.primal.data.account.local.dao

import androidx.room.Entity

@Entity(primaryKeys = ["permissionId", "connectionId"])
data class AppPermissionData(
    val permissionId: String,
    val connectionId: String,
    val action: PermissionAction,
)

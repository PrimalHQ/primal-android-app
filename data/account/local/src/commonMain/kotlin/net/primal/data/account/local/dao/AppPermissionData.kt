package net.primal.data.account.local.dao

import androidx.room.Entity

@Entity(primaryKeys = ["permissionId", "clientPubKey"])
data class AppPermissionData(
    val permissionId: String,
    val clientPubKey: String,
    val action: PermissionAction,
)

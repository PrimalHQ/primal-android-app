package net.primal.data.account.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.shared.data.local.encryption.Encryptable

@Entity
data class AppPermissionData(
    @PrimaryKey
    val permissionId: String,
    val connectionId: String,
    val permissionName: Encryptable<String>,
    val defaultAction: PermissionAction,
)

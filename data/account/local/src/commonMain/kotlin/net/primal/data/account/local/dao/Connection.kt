package net.primal.data.account.local.dao

import androidx.room.Relation

data class Connection(
    val data: ConnectionData,

    @Relation(
        parentColumn = "connectionId",
        entityColumn = "connectionId",
    )
    val permissions: List<PermissionData>,
)

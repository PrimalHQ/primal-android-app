package net.primal.data.account.local.dao

import androidx.room.Relation

data class AppConnection(
    val data: AppConnectionData,

    @Relation(
        parentColumn = "connectionId",
        entityColumn = "connectionId",
    )
    val permissions: List<AppPermissionData>,
)

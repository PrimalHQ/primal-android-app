package net.primal.data.account.local.dao

import androidx.room.Embedded
import androidx.room.Relation

data class AppConnection(
    @Embedded
    val data: AppConnectionData,

    @Relation(
        parentColumn = "connectionId",
        entityColumn = "connectionId",
    )
    val permissions: List<AppPermissionData>,
)

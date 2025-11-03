package net.primal.data.account.local.dao

import androidx.room.Embedded
import androidx.room.Relation

data class AppSession(
    @Embedded
    val data: AppSessionData,

    @Relation(
        parentColumn = "connectionId",
        entityColumn = "connectionId",
    )
    val connection: AppConnectionData,

    @Relation(
        parentColumn = "connectionId",
        entityColumn = "connectionId",
    )
    val permissions: List<AppPermissionData>,
)

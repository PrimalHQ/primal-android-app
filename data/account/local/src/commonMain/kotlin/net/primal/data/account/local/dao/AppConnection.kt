package net.primal.data.account.local.dao

import androidx.room.Embedded
import androidx.room.Relation

data class AppConnection(
    @Embedded
    val data: AppConnectionData,

    @Relation(
        parentColumn = "clientPubKey",
        entityColumn = "clientPubKey",
    )
    val permissions: List<AppPermissionData>,
)

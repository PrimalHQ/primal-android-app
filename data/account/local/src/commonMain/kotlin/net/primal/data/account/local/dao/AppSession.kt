package net.primal.data.account.local.dao

import androidx.room.Embedded
import androidx.room.Relation

data class AppSession(
    @Embedded
    val data: AppSessionData,

    @Relation(
        parentColumn = "clientPubKey",
        entityColumn = "clientPubKey",
    )
    val connection: AppConnectionData,

    @Relation(
        parentColumn = "clientPubKey",
        entityColumn = "clientPubKey",
    )
    val permissions: List<AppPermissionData>,
)

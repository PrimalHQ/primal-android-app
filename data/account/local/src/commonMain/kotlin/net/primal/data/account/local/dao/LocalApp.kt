package net.primal.data.account.local.dao

import androidx.room.Embedded
import androidx.room.Relation

data class LocalApp(
    @Embedded
    val data: LocalAppData,

    @Relation(
        parentColumn = "packageName",
        entityColumn = "clientPubKey",
    )
    val permissions: List<AppPermissionData>,
)

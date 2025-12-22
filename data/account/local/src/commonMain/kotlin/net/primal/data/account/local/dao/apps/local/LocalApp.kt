package net.primal.data.account.local.dao.apps.local

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.data.account.local.dao.apps.AppPermissionData

data class LocalApp(
    @Embedded
    val data: LocalAppData,

    @Relation(
        parentColumn = "identifier",
        entityColumn = "clientPubKey",
    )
    val permissions: List<AppPermissionData>,
)

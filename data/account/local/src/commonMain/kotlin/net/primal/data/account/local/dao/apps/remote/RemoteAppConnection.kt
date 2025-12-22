package net.primal.data.account.local.dao.apps.remote

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.data.account.local.dao.apps.AppPermissionData

data class RemoteAppConnection(
    @Embedded
    val data: RemoteAppConnectionData,

    @Relation(
        parentColumn = "clientPubKey",
        entityColumn = "clientPubKey",
    )
    val permissions: List<AppPermissionData>,
)

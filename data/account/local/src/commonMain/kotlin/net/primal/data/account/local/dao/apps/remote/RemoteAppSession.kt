package net.primal.data.account.local.dao.apps.remote

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.data.account.local.dao.apps.AppPermissionData
import net.primal.data.account.local.dao.apps.AppSessionData

data class RemoteAppSession(
    @Embedded
    val data: AppSessionData,

    @Relation(
        parentColumn = "appIdentifier",
        entityColumn = "clientPubKey",
    )
    val connection: RemoteAppConnectionData,

    @Relation(
        parentColumn = "appIdentifier",
        entityColumn = "appIdentifier",
    )
    val permissions: List<AppPermissionData>,
)

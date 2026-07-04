package net.primal.data.account.local.dao.apps.remote

import androidx.room3.Embedded
import androidx.room3.Relation
import net.primal.data.account.local.dao.apps.AppPermissionData

data class RemoteAppConnection(
    @Embedded
    val data: RemoteAppConnectionData,

    @Relation(
        parentColumns = ["clientPubKey"],
        entityColumns = ["appIdentifier"],
    )
    val permissions: List<AppPermissionData>,
)

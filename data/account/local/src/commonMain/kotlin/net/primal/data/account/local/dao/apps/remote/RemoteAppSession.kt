package net.primal.data.account.local.dao.apps.remote

import androidx.room3.Embedded
import androidx.room3.Relation
import net.primal.data.account.local.dao.apps.AppPermissionData
import net.primal.data.account.local.dao.apps.AppSessionData

data class RemoteAppSession(
    @Embedded
    val data: AppSessionData,

    @Relation(
        parentColumns = ["appIdentifier"],
        entityColumns = ["clientPubKey"],
    )
    val connection: RemoteAppConnectionData,

    @Relation(
        parentColumns = ["appIdentifier"],
        entityColumns = ["appIdentifier"],
    )
    val permissions: List<AppPermissionData>,
)

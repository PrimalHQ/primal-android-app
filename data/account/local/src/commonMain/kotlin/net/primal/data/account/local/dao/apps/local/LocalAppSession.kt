package net.primal.data.account.local.dao.apps.local

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.data.account.local.dao.apps.AppPermissionData
import net.primal.data.account.local.dao.apps.AppSessionData

data class LocalAppSession(
    @Embedded
    val data: AppSessionData,

    @Relation(
        parentColumn = "appIdentifier",
        entityColumn = "identifier",
    )
    val app: LocalAppData,

    @Relation(
        parentColumn = "appIdentifier",
        entityColumn = "appIdentifier",
    )
    val permissions: List<AppPermissionData>,
)

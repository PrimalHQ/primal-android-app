package net.primal.data.account.local.dao.apps.local

import androidx.room3.Embedded
import androidx.room3.Relation
import net.primal.data.account.local.dao.apps.AppPermissionData

data class LocalApp(
    @Embedded
    val data: LocalAppData,

    @Relation(
        parentColumns = ["identifier"],
        entityColumns = ["appIdentifier"],
    )
    val permissions: List<AppPermissionData>,
)

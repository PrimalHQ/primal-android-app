package net.primal.data.account.repository.mappers

import net.primal.data.account.local.dao.LocalApp as LocalAppPO
import net.primal.data.account.local.dao.LocalAppData
import net.primal.domain.account.model.LocalApp

fun LocalApp.asPO() =
    LocalAppData(
        identifier = identifier,
        packageName = this.packageName,
        userPubKey = userPubKey,
        trustLevel = trustLevel.asPO(),
    )

fun LocalAppPO.asDomain() =
    LocalApp(
        identifier = this.data.identifier,
        packageName = this.data.packageName,
        userPubKey = this.data.userPubKey,
        trustLevel = this.data.trustLevel.asDomain(),
        permissions = this.permissions.map { it.asDomain() },
    )

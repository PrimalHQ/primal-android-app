package net.primal.data.account.repository.mappers

import net.primal.data.account.local.dao.LocalApp as LocalAppPO
import net.primal.data.account.local.dao.LocalAppData
import net.primal.domain.account.model.LocalApp
import net.primal.shared.data.local.encryption.asEncryptable

fun LocalApp.asPO() =
    LocalAppData(
        identifier = identifier,
        packageName = this.packageName,
        userPubKey = userPubKey,
        image = image?.asEncryptable(),
        name = name?.asEncryptable(),
        trustLevel = trustLevel.asPO(),
    )

fun LocalAppPO.asDomain() =
    LocalApp(
        identifier = this.data.identifier,
        packageName = this.data.packageName,
        userPubKey = this.data.userPubKey,
        image = this.data.image?.decrypted,
        name = this.data.name?.decrypted,
        trustLevel = this.data.trustLevel.asDomain(),
        permissions = this.permissions.map { it.asDomain() },
    )

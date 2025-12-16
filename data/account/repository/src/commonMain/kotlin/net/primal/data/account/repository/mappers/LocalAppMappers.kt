package net.primal.data.account.repository.mappers

import net.primal.data.account.local.dao.LocalAppData
import net.primal.domain.account.model.LocalApp
import net.primal.shared.data.local.encryption.asEncryptable


fun LocalApp.asPO() =
    LocalAppData(
        packageName = this.packageName,
        userPubKey = userPubKey.asEncryptable(),
        image = image.asEncryptable(),
        name = name.asEncryptable(),
        trustLevel = trustLevel.asPO(),
    )

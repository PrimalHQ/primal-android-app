package net.primal.data.account.repository.mappers

import net.primal.data.account.local.dao.AppConnection as AppConnectionPO
import net.primal.data.account.local.dao.AppPermissionData as AppPermissionDataPO
import net.primal.data.account.local.dao.PermissionAction as PermissionActionPO
import net.primal.data.account.local.dao.TrustLevel as TrustLevelPO
import net.primal.domain.account.model.AppConnection as AppConnectionDO
import net.primal.domain.account.model.AppPermission as AppPermissionDO
import net.primal.domain.account.model.PermissionAction as PermissionActionDO
import net.primal.domain.account.model.TrustLevel as TrustLevelDO

fun AppConnectionPO.asDomain() =
    AppConnectionDO(
        clientPubKey = this.data.clientPubKey,
        userPubKey = this.data.userPubKey.decrypted,
        signerPubKey = this.data.signerPubKey.decrypted,
        relays = this.data.relays.decrypted,
        name = this.data.name?.decrypted,
        url = this.data.url?.decrypted,
        image = this.data.image?.decrypted,
        permissions = this.permissions.map { it.asDomain() },
        autoStart = this.data.autoStart,
        trustLevel = this.data.trustLevel.asDomain(),
    )

fun AppPermissionDataPO.asDomain() =
    AppPermissionDO(
        permissionId = this.permissionId,
        clientPubKey = this.clientPubKey,
        action = this.action.asDomain(),
    )

fun AppPermissionDO.asPO() =
    AppPermissionDataPO(
        permissionId = this.permissionId,
        clientPubKey = this.clientPubKey,
        action = this.action.asPO(),
    )

fun PermissionActionDO.asPO() =
    when (this) {
        PermissionActionDO.Approve -> PermissionActionPO.Approve
        PermissionActionDO.Deny -> PermissionActionPO.Deny
        PermissionActionDO.Ask -> PermissionActionPO.Ask
    }

fun PermissionActionPO.asDomain() =
    when (this) {
        PermissionActionPO.Approve -> PermissionActionDO.Approve
        PermissionActionPO.Deny -> PermissionActionDO.Deny
        PermissionActionPO.Ask -> PermissionActionDO.Ask
    }

fun TrustLevelPO.asDomain() =
    when (this) {
        TrustLevelPO.Full -> TrustLevelDO.Full
        TrustLevelPO.Medium -> TrustLevelDO.Medium
        TrustLevelPO.Low -> TrustLevelDO.Low
    }

fun TrustLevelDO.asPO() =
    when (this) {
        TrustLevelDO.Full -> TrustLevelPO.Full
        TrustLevelDO.Medium -> TrustLevelPO.Medium
        TrustLevelDO.Low -> TrustLevelPO.Low
    }

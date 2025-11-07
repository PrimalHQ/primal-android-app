package net.primal.data.account.repository.mappers

import net.primal.data.account.local.dao.AppConnection as AppConnectionPO
import net.primal.data.account.local.dao.AppPermissionData as AppPermissionDataPO
import net.primal.data.account.local.dao.PermissionAction as PermissionActionPO
import net.primal.domain.account.model.AppConnection as AppConnectionDO
import net.primal.domain.account.model.AppPermission as AppPermissionDO
import net.primal.domain.account.model.PermissionAction as PermissionActionDO

fun AppConnectionPO.asDomain() =
    AppConnectionDO(
        connectionId = this.data.connectionId,
        userPubKey = this.data.userPubKey.decrypted,
        clientPubKey = this.data.clientPubKey.decrypted,
        signerPubKey = this.data.signerPubKey.decrypted,
        relays = this.data.relays.decrypted,
        name = this.data.name?.decrypted,
        url = this.data.url?.decrypted,
        image = this.data.image?.decrypted,
        permissions = this.permissions.map { it.asDomain() },
        autoStart = this.data.autoStart,
    )

fun AppPermissionDataPO.asDomain() =
    AppPermissionDO(
        permissionId = this.permissionId,
        permissionName = this.permissionName.decrypted,
        defaultAction = this.defaultAction.asDomain(),
    )

fun PermissionActionPO.asDomain() =
    when (this) {
        PermissionActionPO.Approve -> PermissionActionDO.Approve
        PermissionActionPO.Deny -> PermissionActionDO.Deny
        PermissionActionPO.Ask -> PermissionActionDO.Ask
    }

package net.primal.data.account.repository.mappers

import net.primal.data.account.local.dao.AppSession as AppSessionPO
import net.primal.domain.account.model.AppSession as AppSessionDO
import net.primal.domain.account.model.AppSessionState

fun AppSessionPO.asDomain() =
    AppSessionDO(
        sessionId = this.data.sessionId,
        userPubKey = this.connection.userPubKey,
        clientPubKey = this.connection.clientPubKey,
        signerPubKey = this.connection.signerPubKey,
        relays = this.connection.relays.decrypted,
        name = this.connection.name?.decrypted,
        url = this.connection.url?.decrypted,
        image = this.connection.image?.decrypted,
        permissions = this.permissions.map { it.asDomain() },
        sessionStartedAt = this.data.startedAt,
        sessionEndedAt = this.data.endedAt,
        sessionState = when {
            this.data.endedAt != null -> AppSessionState.Ended
            this.data.activeRelayCount > 0 -> AppSessionState.Active
            else -> AppSessionState.Connecting
        },
    )

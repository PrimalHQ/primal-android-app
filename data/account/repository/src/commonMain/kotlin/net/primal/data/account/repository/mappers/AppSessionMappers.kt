package net.primal.data.account.repository.mappers

import net.primal.data.account.local.dao.apps.AppSessionData
import net.primal.data.account.local.dao.apps.AppSessionType as AppSessionTypePO
import net.primal.domain.account.model.AppSession
import net.primal.domain.account.model.AppSessionState
import net.primal.domain.account.model.AppSessionType as AppSessionTypeDO

fun AppSessionData.asDomain() =
    AppSession(
        sessionId = this.sessionId,
        appIdentifier = this.appIdentifier,
        sessionStartedAt = this.startedAt,
        sessionEndedAt = this.endedAt,
        sessionState = when {
            this.endedAt != null -> AppSessionState.Ended
            else -> AppSessionState.Active
        },
        sessionType = when (this.sessionType) {
            AppSessionTypePO.LocalSession -> AppSessionTypeDO.Local
            AppSessionTypePO.RemoteSession -> AppSessionTypeDO.Remote
        },
    )

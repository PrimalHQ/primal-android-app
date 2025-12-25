package net.primal.data.account.repository.mappers

import net.primal.data.account.local.dao.apps.local.LocalAppSession as LocalAppSessionPO
import net.primal.domain.account.model.LocalAppSession as LocalAppSessionDO
import net.primal.domain.account.model.LocalAppSessionState

fun LocalAppSessionPO.asDomain() =
    LocalAppSessionDO(
        sessionId = this.data.sessionId,
        appPackageName = this.app.packageName,
        userPubKey = this.app.userPubKey,
        trustLevel = this.app.trustLevel.asDomain(),
        permissions = this.permissions.map { it.asDomain() },
        sessionStartedAt = this.data.startedAt,
        sessionEndedAt = this.data.endedAt,
        sessionState = if (this.data.endedAt == null) LocalAppSessionState.Active else LocalAppSessionState.Ended,
    )

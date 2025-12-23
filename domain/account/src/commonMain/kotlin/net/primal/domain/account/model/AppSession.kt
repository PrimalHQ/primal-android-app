package net.primal.domain.account.model

data class AppSession(
    val sessionId: String,
    val appIdentifier: String,
    val sessionType: AppSessionType,
    val sessionStartedAt: Long,
    val sessionEndedAt: Long?,
    val sessionState: AppSessionState,
)

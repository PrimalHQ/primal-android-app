package net.primal.domain.account.model

data class LocalAppSession(
    val sessionId: String,
    val appPackageName: String,
    val userPubKey: String,
    val trustLevel: TrustLevel,
    val permissions: List<AppPermission>,
    val sessionStartedAt: Long,
    val sessionEndedAt: Long?,
    val sessionState: LocalAppSessionState,
)

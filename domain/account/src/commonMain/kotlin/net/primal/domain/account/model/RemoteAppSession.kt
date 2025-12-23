package net.primal.domain.account.model

data class RemoteAppSession(
    val sessionId: String,
    val userPubKey: String,
    val clientPubKey: String,
    val signerPubKey: String,
    val relays: List<String>,
    val name: String?,
    val url: String?,
    val image: String?,
    val permissions: List<AppPermission>,
    val sessionStartedAt: Long,
    val sessionEndedAt: Long?,
    val sessionState: RemoteAppSessionState,
)

package net.primal.domain.account.model

import kotlin.time.Instant

data class RemoteAppConnectionState(
    val status: RemoteAppConnectionStatus,
    val activeSessionId: String,
    val totalRelayCount: Int,
    val connectedRelayCount: Int,
    val lastError: RemoteAppConnectionError? = null,
    val lastConnectedAt: Instant? = null,
)

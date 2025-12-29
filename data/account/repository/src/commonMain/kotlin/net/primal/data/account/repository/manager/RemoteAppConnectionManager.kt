package net.primal.data.account.repository.manager

import kotlin.time.Clock
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import net.primal.domain.account.model.RemoteAppConnectionError
import net.primal.domain.account.model.RemoteAppConnectionState
import net.primal.domain.account.model.RemoteAppConnectionStatus

class RemoteAppConnectionManager {
    private val _sessionStates = MutableStateFlow<Map<String, RemoteAppConnectionState>>(emptyMap())
    val sessionStates: StateFlow<Map<String, RemoteAppConnectionState>> = _sessionStates.asStateFlow()

    /**
     * Relay states are tracked per session because each session may have a different set of required relays.
     * Even though underlying relay connections are shared/reused across sessions for resource optimization,
     * the logical connection state (connected relay count, all-relays-failed errors, etc.) is session-specific.
     */
    private val relayStates = atomic(mutableMapOf<String, MutableMap<String, RelayConnectionState>>())

    private sealed class RelayConnectionState {
        object Connected : RelayConnectionState()
        object Connecting : RelayConnectionState()
        data class Disconnected(val error: Throwable?) : RelayConnectionState()
    }

    private fun updateRelayState(
        sessionId: String,
        relayUrl: String,
        state: RelayConnectionState,
    ) {
        relayStates.update { currentMap ->
            currentMap.toMutableMap().apply {
                val sessionRelays = getOrPut(sessionId) { mutableMapOf() }
                sessionRelays[relayUrl] = state
            }
        }
    }

    fun observeSessionConnectionState(sessionId: String): Flow<RemoteAppConnectionState?> {
        return sessionStates
            .map { it[sessionId] }
            .distinctUntilChanged()
    }

    fun onRelayConnected(sessionId: String, relayUrl: String) {
        updateRelayState(sessionId, relayUrl, RelayConnectionState.Connected)
        recomputeSessionState(sessionId)
    }

    fun onRelayConnected(sessionIds: List<String>, relayUrl: String) {
        sessionIds.forEach { onRelayConnected(sessionId = it, relayUrl = relayUrl) }
    }

    fun onRelayDisconnected(
        sessionId: String,
        relayUrl: String,
        error: Throwable?,
    ) {
        updateRelayState(sessionId, relayUrl, RelayConnectionState.Disconnected(error))
        recomputeSessionState(sessionId)
    }

    fun onRelayDisconnected(
        sessionIds: List<String>,
        relayUrl: String,
        error: Throwable?,
    ) {
        sessionIds.forEach { onRelayDisconnected(sessionId = it, relayUrl = relayUrl, error = error) }
    }

    fun onRelayConnecting(sessionId: String, relayUrl: String) {
        updateRelayState(sessionId, relayUrl, RelayConnectionState.Connecting)
        recomputeSessionState(sessionId)
    }

    private fun recomputeSessionState(sessionId: String) {
        val relays = relayStates.value[sessionId] ?: return
        val connectedCount = relays.count { it.value == RelayConnectionState.Connected }
        val connectingCount = relays.count { it.value is RelayConnectionState.Connecting }
        val totalCount = relays.size

        val status = when {
            connectedCount > 0 -> RemoteAppConnectionStatus.Connected
            connectingCount > 0 -> RemoteAppConnectionStatus.Connecting
            else -> RemoteAppConnectionStatus.Disconnected
        }

        val error = if (status == RemoteAppConnectionStatus.Disconnected && totalCount > 0) {
            RemoteAppConnectionError.AllRelaysFailed("All $totalCount relays failed to connect")
        } else {
            null
        }

        _sessionStates.update { current ->
            val existing = current[sessionId]
            current + (
                sessionId to RemoteAppConnectionState(
                    status = status,
                    activeSessionId = sessionId,
                    totalRelayCount = totalCount,
                    connectedRelayCount = connectedCount,
                    lastError = error,
                    lastConnectedAt = if (status == RemoteAppConnectionStatus.Connected) {
                        if (existing?.status != RemoteAppConnectionStatus.Connected) {
                            Clock.System.now()
                        } else {
                            existing.lastConnectedAt ?: Clock.System.now()
                        }
                    } else {
                        existing?.lastConnectedAt
                    },
                )
                )
        }
    }
}

package net.primal.android.nostrconnect.approvals

import net.primal.android.nostrconnect.model.ActiveSessionUi
import net.primal.domain.account.model.SessionEvent
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent

interface PermissionsContract {
    data class UiState(
        val bottomSheetVisibility: Boolean = false,
        val requestQueue: List<Pair<ActiveSessionUi, List<SessionEvent>>> = emptyList(),
        val activeSessions: Map<String, ActiveSessionUi> = emptyMap(),
        val responding: Boolean = false,
        val permissionsMap: Map<String, String> = emptyMap(),
        val eventDetailsSessionEvent: SessionEvent? = null,
        val parsedSignedEvent: NostrEvent? = null,
        val parsedUnsignedEvent: NostrUnsignedEvent? = null,
    ) {
        val session = requestQueue.firstOrNull()?.first
        val sessionEvents = requestQueue.firstOrNull()?.second ?: emptyList()
    }

    sealed class UiEvent {
        data object DismissSheet : UiEvent()
        data class Allow(val eventIds: List<String>, val alwaysAllow: Boolean) : UiEvent()
        data class Reject(val eventIds: List<String>, val alwaysReject: Boolean) : UiEvent()
        data class OpenEventDetails(val eventId: String) : UiEvent()
        data object CloseEventDetails : UiEvent()
    }
}

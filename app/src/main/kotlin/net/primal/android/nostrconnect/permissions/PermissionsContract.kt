package net.primal.android.nostrconnect.permissions

import net.primal.android.nostrconnect.model.ActiveSessionUi
import net.primal.domain.account.model.SessionEvent
import net.primal.domain.nostr.NostrEvent

interface PermissionsContract {
    data class UiState(
        val bottomSheetVisibility: Boolean = false,
        val selectedEventIds: Set<String> = emptySet(),
        val requestQueue: List<Pair<ActiveSessionUi, List<SessionEvent>>> = emptyList(),
        val activeSessions: Map<String, ActiveSessionUi> = emptyMap(),
        val responding: Boolean = false,
        val eventDetails: SessionEvent? = null,
        val eventDetailsNostrEvent: NostrEvent? = null,
    ) {
        val session = requestQueue.firstOrNull()?.first
        val sessionEvents = requestQueue.firstOrNull()?.second ?: emptyList()
    }

    sealed class UiEvent {
        data object DismissSheet : UiEvent()
        data object SelectAll : UiEvent()
        data object DeselectAll : UiEvent()
        data class SelectEvent(val eventId: String) : UiEvent()
        data class DeselectEvent(val eventId: String) : UiEvent()
        data class AllowSelected(val alwaysAllow: Boolean) : UiEvent()
        data class RejectSelected(val alwaysReject: Boolean) : UiEvent()
        data class OpenEventDetails(val eventId: String) : UiEvent()
        data object CloseEventDetails : UiEvent()
    }
}

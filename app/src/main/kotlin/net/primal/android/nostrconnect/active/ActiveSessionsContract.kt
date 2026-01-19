package net.primal.android.nostrconnect.active

import net.primal.android.core.errors.UiError
import net.primal.android.nostrconnect.model.ActiveSessionUi

interface ActiveSessionsContract {
    data class UiState(
        val sessions: List<ActiveSessionUi> = emptyList(),
        val selectedSessions: Set<String> = emptySet(),
        val disconnecting: Boolean = false,
        val reconnecting: Boolean = false,
        val error: UiError? = null,
    ) {
        val allSessionsSelected: Boolean get() = sessions.isNotEmpty() && selectedSessions.size == sessions.size
    }

    sealed class UiEvent {
        data class SessionClick(val sessionId: String) : UiEvent()
        data object SelectAllClick : UiEvent()
        data object DisconnectClick : UiEvent()
        data object ReconnectClick : UiEvent()
        data object DismissError : UiEvent()
    }

    sealed class SideEffect {
        data object SessionsDisconnected : SideEffect()
    }
}

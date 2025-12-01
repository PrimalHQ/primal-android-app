package net.primal.android.nostrconnect.list

import net.primal.android.core.errors.UiError
import net.primal.android.nostrconnect.model.ActiveSessionUi

interface ActiveSessionsContract {
    data class UiState(
        val sessions: List<ActiveSessionUi> = emptyList(),
        val selectedSessions: Set<String> = emptySet(),
        val disconnecting: Boolean = false,
        val error: UiError? = null,
    ) {
        val allSessionsSelected: Boolean get() = sessions.isNotEmpty() && selectedSessions.size == sessions.size
    }

    sealed class UiEvent {
        data class SessionClick(val sessionId: String) : UiEvent()
        data object SelectAllClick : UiEvent()
        data object DisconnectClick : UiEvent()
        data object SettingsClick : UiEvent()
        data object DismissError : UiEvent()
    }

    sealed class SideEffect {
        data object SessionsDisconnected : SideEffect()
        data class NavigateToConnectedApps(val connectionId: String? = null) : SideEffect()
    }
}

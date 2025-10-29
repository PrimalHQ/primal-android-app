package net.primal.android.nostrconnect.list

import net.primal.android.core.errors.UiError
import net.primal.android.drawer.multiaccount.model.UserAccountUi

interface ActiveSessionsContract {
    data class UiState(
        val sessions: List<NwcSessionUi> = emptyList(),
        val selectedSessions: Set<String> = emptySet(),
        val disconnecting: Boolean = false,
        val error: UiError? = null,
    ) {
        val allSessionsSelected: Boolean get() = sessions.isNotEmpty() && selectedSessions.size == sessions.size
    }

    data class NwcSessionUi(
        val connectionId: String,
        val appName: String?,
        val appUrl: String?,
        val appImageUrl: String?,
        val userAccount: UserAccountUi,
    )

    sealed class UiEvent {
        data class SessionClick(val connectionId: String) : UiEvent()
        data object SelectAllClick : UiEvent()
        data object DisconnectClick : UiEvent()
        data object DismissError : UiEvent()
    }

    sealed class SideEffect {
        data object SessionsDisconnected : SideEffect()
    }
}

package net.primal.android.settings.connected.details

import net.primal.android.core.errors.UiError
import net.primal.android.settings.connected.model.SessionUi

interface ConnectedAppDetailsContract {
    data class UiState(
        val loading: Boolean = true,
        val appName: String? = null,
        val appIconUrl: String? = null,
        val isSessionActive: Boolean = false,
        val lastSessionStartedAt: Long? = null,
        val autoStartSession: Boolean = false,
        val recentSessions: List<SessionUi> = emptyList(),
        val confirmingDeletion: Boolean = false,
        val editingName: Boolean = false,
        val error: UiError? = null,
    )

    sealed class UiEvent {
        data object StartSession : UiEvent()
        data object EndSession : UiEvent()
        data object DeleteConnection : UiEvent()
        data object ConfirmDeletion : UiEvent()
        data object DismissDeletionConfirmation : UiEvent()
        data object EditName : UiEvent()
        data class NameChange(val name: String) : UiEvent()
        data object DismissEditNameDialog : UiEvent()
        data class AutoStartSessionChange(val enabled: Boolean) : UiEvent()
        data object DismissError : UiEvent()
        data class SessionClick(val sessionId: String) : UiEvent()
    }

    sealed class SideEffect {
        data object ConnectionDeleted : SideEffect()
        data class NavigateToSessionDetails(val connectionId: String, val sessionId: String) : SideEffect()
    }
}

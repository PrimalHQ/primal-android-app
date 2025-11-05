package net.primal.android.settings.connected.details

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
        val activeSessionId: String? = null,
    )

    data class SessionUi(
        val sessionId: String,
        val startedAt: Long,
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
    }

    sealed class SideEffect {
        data object ConnectionDeleted : SideEffect()
    }
}

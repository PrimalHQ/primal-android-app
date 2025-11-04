package net.primal.android.settings.connected.details

import net.primal.android.core.errors.UiError
import net.primal.domain.links.CdnImage

interface ConnectedAppDetailsContract {
    data class UiState(
        val loading: Boolean = true,
        val appName: String = "",
        val appIconUrl: String? = null,
        val userAvatarCdnImage: CdnImage? = null,
        val isSessionActive: Boolean = false,
        val lastSession: Long? = null,
        val autoStartSession: Boolean = false,
        val recentSessions: List<SessionUi> = emptyList(),
        val error: UiError? = null,
        val confirmingDeletion: Boolean = false,
        val editingName: Boolean = false,
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
        data object DismissError : UiEvent()
    }

    sealed class SideEffect {
        data object ConnectionDelete : SideEffect()
    }
}

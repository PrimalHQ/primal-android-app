package net.primal.android.settings.connected.details

import net.primal.android.core.errors.UiError
import net.primal.android.settings.connected.model.SessionUi
import net.primal.domain.account.model.TrustLevel

interface ConnectedAppDetailsContract {
    data class UiState(
        val connectionId: String,
        val loading: Boolean = true,
        val appName: String? = null,
        val appIconUrl: String? = null,
        val isSessionActive: Boolean = false,
        val lastSessionStartedAt: Long? = null,
        val autoStartSession: Boolean = false,
        val trustLevel: TrustLevel = TrustLevel.Low,
        val recentSessions: List<SessionUi> = emptyList(),
        val error: UiError? = null,
    )

    sealed class UiEvent {
        data object StartSession : UiEvent()
        data object EndSession : UiEvent()
        data object DeleteConnection : UiEvent()
        data class EditName(val name: String) : UiEvent()
        data class AutoStartSessionChange(val enabled: Boolean) : UiEvent()
        data class UpdateTrustLevel(val trustLevel: TrustLevel) : UiEvent()
        data object DismissError : UiEvent()
    }

    sealed class SideEffect {
        data object ConnectionDeleted : SideEffect()
    }
}

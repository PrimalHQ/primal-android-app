package net.primal.android.settings.connected.details.local

import net.primal.android.core.errors.UiError
import net.primal.android.settings.connected.model.SessionUi
import net.primal.domain.account.model.TrustLevel

interface LocalAppDetailsContract {
    data class UiState(
        val identifier: String,
        val loading: Boolean = true,
        val appPackageName: String? = null,
        val appName: String? = null,
        val lastSessionStartedAt: Long? = null,
        val trustLevel: TrustLevel = TrustLevel.Low,
        val recentSessions: List<SessionUi> = emptyList(),
        val error: UiError? = null,
    )

    sealed class UiEvent {
        data object DeleteConnection : UiEvent()
        data class UpdateTrustLevel(val trustLevel: TrustLevel) : UiEvent()
        data object DismissError : UiEvent()
    }

    sealed class SideEffect {
        data object ConnectionDeleted : SideEffect()
    }
}

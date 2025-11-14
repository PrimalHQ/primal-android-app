package net.primal.android.settings.connected.session

import net.primal.android.settings.connected.model.SessionEventUi

interface SessionDetailsContract {
    data class UiState(
        val loading: Boolean = true,
        val appName: String? = null,
        val appIconUrl: String? = null,
        val sessionStartedAt: Long? = null,
        val sessionEvents: List<SessionEventUi> = emptyList(),
    )

    sealed class UiEvent {
        data class EventClick(val eventId: String) : UiEvent()
    }

    sealed class SideEffect {
        data class NavigateToEventDetails(
            val connectionId: String,
            val sessionId: String,
            val eventId: String,
        ) : SideEffect()
    }
}

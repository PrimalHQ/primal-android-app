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
}

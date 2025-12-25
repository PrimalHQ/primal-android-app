package net.primal.android.settings.connected.session.remote

import net.primal.android.settings.connected.model.SessionEventUi

interface RemoteSessionDetailsContract {
    data class UiState(
        val loading: Boolean = true,
        val sessionId: String = "",
        val appName: String? = null,
        val appIconUrl: String? = null,
        val sessionStartedAt: Long? = null,
        val sessionEvents: List<SessionEventUi> = emptyList(),
        val namingMap: Map<String, String> = emptyMap(),
    )
}

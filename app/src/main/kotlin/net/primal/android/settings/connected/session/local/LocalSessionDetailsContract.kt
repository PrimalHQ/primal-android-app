package net.primal.android.settings.connected.session.local

import net.primal.android.settings.connected.model.SessionEventUi

interface LocalSessionDetailsContract {
    data class UiState(
        val sessionId: String,
        val loading: Boolean = true,
        val appPackageName: String? = null,
        val appName: String? = null,
        val sessionStartedAt: Long? = null,
        val sessionEvents: List<SessionEventUi> = emptyList(),
        val namingMap: Map<String, String> = emptyMap(),
    )
}

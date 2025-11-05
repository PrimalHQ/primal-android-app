package net.primal.android.settings.connected

import net.primal.android.settings.connected.model.AppConnectionUi

interface ConnectedAppsContract {
    data class UiState(
        val connections: List<AppConnectionUi> = emptyList(),
        val activeConnectionIds: Set<String> = emptySet(),
        val loading: Boolean = true,
    )

    sealed class UiEvent {
        data class ConnectionClick(val connectionId: String) : UiEvent()
    }
}

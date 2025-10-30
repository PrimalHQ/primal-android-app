package net.primal.android.settings.connected

import net.primal.android.settings.connected.model.AppConnectionUi

interface ConnectedAppsContract {
    data class UiState(
        val connections: List<AppConnectionUi> = emptyList(),
        val loading: Boolean = true,
    )

    sealed class UiEvent {
        data class ConnectionClicked(val connectionId: String) : UiEvent()
    }
}

package net.primal.android.settings.connected

import net.primal.android.settings.connected.model.AppConnectionUi

interface ConnectedAppsContract {
    data class UiState(
        val localConnections: List<AppConnectionUi> = emptyList(),
        val remoteConnections: List<AppConnectionUi> = emptyList(),
        val activeClientPubKeys: Set<String> = emptySet(),
        val loading: Boolean = true,
    )
}

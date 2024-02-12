package net.primal.android.settings.network

import net.primal.android.user.domain.Relay

interface NetworkSettingsContract {
    data class UiState(
        val relays: List<Relay> = emptyList(),
        val cachingUrl: String = "",
        val addingRelay: Boolean = false,
    )

    sealed class UiEvent {
        data object RestoreDefaultRelays : UiEvent()
        data class DeleteRelay(val url: String) : UiEvent()
        data class AddRelay(val url: String) : UiEvent()
    }
}

package net.primal.android.settings.network

interface NetworkSettingsContract {
    data class UiState(
        val relays: List<SocketDestinationUiState> = emptyList(),
        val cachingService: SocketDestinationUiState? = null,
        val addingRelay: Boolean = false,
    )

    sealed class UiEvent {
        data object RestoreDefaultRelays : UiEvent()
        data class DeleteRelay(val url: String) : UiEvent()
        data class AddRelay(val url: String) : UiEvent()
    }
}

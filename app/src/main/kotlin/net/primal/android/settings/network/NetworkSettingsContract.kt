package net.primal.android.settings.network

interface NetworkSettingsContract {
    data class UiState(
        val relays: List<SocketDestinationUiState> = emptyList(),
        val cachingService: SocketDestinationUiState? = null,
        val working: Boolean = false,
        val newRelayUrl: String = "",
        val error: NetworkSettingsError? = null,
    ) {
        sealed class NetworkSettingsError {
            data class FailedToAddRelay(val cause: Throwable?) : NetworkSettingsError()
        }
    }

    sealed class UiEvent {
        data object RestoreDefaultRelays : UiEvent()
        data object DismissError : UiEvent()
        data class DeleteRelay(val url: String) : UiEvent()
        data class ConfirmAddRelay(val url: String) : UiEvent()
        data class UpdateNewRelayUrl(val url: String) : UiEvent()
    }
}

package net.primal.android.settings.network

import net.primal.android.namecoin.NamecoinSettings

interface NetworkSettingsContract {
    data class UiState(
        val relays: List<SocketDestinationUiState> = emptyList(),
        val cachingService: SocketDestinationUiState? = null,
        val cachingProxyEnabled: Boolean = false,
        val updatingRelays: Boolean = false,
        val updatingCachingService: Boolean = false,
        val newRelayUrl: String = "",
        val newCachingServiceUrl: String = "",
        val namecoinSettings: NamecoinSettings = NamecoinSettings.DEFAULT,
        val error: NetworkSettingsError? = null,
    ) {
        sealed class NetworkSettingsError {
            data class FailedToAddRelay(val cause: Throwable?) : NetworkSettingsError()
        }
    }

    sealed class UiEvent {
        data object RestoreDefaultRelays : UiEvent()
        data object RestoreDefaultCachingService : UiEvent()
        data object DismissError : UiEvent()
        data class DeleteRelay(val url: String) : UiEvent()
        data class ConfirmRelayInsert(val url: String) : UiEvent()
        data class ConfirmCachingServiceChange(val url: String) : UiEvent()
        data class UpdateNewRelayUrl(val url: String) : UiEvent()
        data class UpdateNewCachingServiceUrl(val url: String) : UiEvent()
        data class UpdateCachingProxyFlag(val enabled: Boolean) : UiEvent()
        data class NamecoinToggleEnabled(val enabled: Boolean) : UiEvent()
        data class NamecoinAddServer(val server: String) : UiEvent()
        data class NamecoinRemoveServer(val server: String) : UiEvent()
        data object NamecoinResetServers : UiEvent()
    }
}

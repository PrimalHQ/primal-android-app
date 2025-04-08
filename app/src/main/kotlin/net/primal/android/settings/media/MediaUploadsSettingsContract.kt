package net.primal.android.settings.media

interface MediaUploadsSettingsContract {
    data class UiState(
        val blossomServerUrl: String = "blossom.primal.net",
        val blossomServerMirrorUrl: String = "blossom.bond",
        val newBlossomServerUrl: String = "",
        val newBlossomServerMirrorUrl: String = "",
        val suggestedBlossomServers: List<String> = listOf<String>("cdn.satellite.earth", "cdn.nostrcheck.me"),
        val blossomMirrorEnabled: Boolean = true,
        val error: MediaUploadsSettingsError? = null,
        val mode: MediaUploadsMode = MediaUploadsMode.View,
    ) {
        sealed class MediaUploadsSettingsError {
            data class FailedToFetch(val error: Throwable) : MediaUploadsSettingsError()
        }
    }

    sealed class UiEvent {
        data class UpdateNewBlossomServerUrl(val url: String) : UiEvent()
        data class ConfirmBlossomServerUrl(val url: String) : UiEvent()
        data class UpdateNewBlossomMirrorServerUrl(val url: String) : UiEvent()
        data class ConfirmBlossomMirrorServerUrl(val url: String) : UiEvent()
        data class UpdateMediaUploadsMode(val mode: MediaUploadsMode) : UiEvent()
        data class UpdateBlossomMirrorEnabled(val enabled: Boolean) : UiEvent()
        data object RestoreDefaultBlossomServer : UiEvent()
    }
}

enum class MediaUploadsMode {
    View,
    EditBlossomServer,
    EditBlossomMirrorServer,
}

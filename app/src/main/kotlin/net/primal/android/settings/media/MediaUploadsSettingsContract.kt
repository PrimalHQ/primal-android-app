package net.primal.android.settings.media

interface MediaUploadsSettingsContract {
    data class UiState(
        val error: MediaUploadsSettingsError? = null,
    ) {
        sealed class MediaUploadsSettingsError {
            data class FailedToFetch(val error: Throwable) : MediaUploadsSettingsError()
        }
    }

    sealed class UiEvent
}

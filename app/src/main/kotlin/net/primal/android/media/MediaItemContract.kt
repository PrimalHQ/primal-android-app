package net.primal.android.media

interface MediaItemContract {
    data class UiState(
        val mediaUrl: String,
        val error: MediaItemError? = null,
    ) {
        sealed class MediaItemError {
            data class FailedToSaveMedia(val cause: Throwable) : MediaItemError()
        }
    }

    sealed class UiEvent {
        data object SaveMedia : UiEvent()
        data object DismissError : UiEvent()
    }

    sealed class SideEffect {
        data object MediaSaved : SideEffect()
    }
}

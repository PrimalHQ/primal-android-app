package net.primal.android.media

import android.graphics.Bitmap

interface MediaItemContract {
    data class UiState(
        val mediaUrl: String,
        val error: MediaItemError? = null,
        val currentDisplayedBitmap: Bitmap? = null,
    ) {
        sealed class MediaItemError {
            data class FailedToSaveMedia(val cause: Throwable) : MediaItemError()
        }
    }

    sealed class UiEvent {
        data class LoadBitmap(val bitmap: Bitmap) : UiEvent()
        data object SaveMedia : UiEvent()
        data object DismissError : UiEvent()
    }

    sealed class SideEffect {
        data object MediaSaved : SideEffect()
    }
}

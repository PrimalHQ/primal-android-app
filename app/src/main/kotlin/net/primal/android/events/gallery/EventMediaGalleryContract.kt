package net.primal.android.events.gallery

import net.primal.android.core.compose.attachment.model.EventUriUi
import net.primal.android.events.domain.EventUriType

interface EventMediaGalleryContract {
    data class UiState(
        val noteId: String,
        val loading: Boolean = true,
        val initialAttachmentIndex: Int = 0,
        val initialPositionMs: Long = 0,
        val error: MediaGalleryError? = null,
        val attachments: List<EventUriUi> = emptyList(),
    ) {
        sealed class MediaGalleryError {
            data class FailedToSaveMedia(val cause: Throwable) : MediaGalleryError()
        }
    }

    sealed class UiEvent {
        data class SaveMedia(val attachment: EventUriUi) : UiEvent()
        data object DismissError : UiEvent()
    }

    sealed class SideEffect {
        data class MediaSaved(val type: EventUriType) : SideEffect()
    }
}

package net.primal.android.attachments.gallery

import net.primal.android.core.compose.attachment.model.NoteAttachmentUi

interface MediaGalleryContract {
    data class UiState(
        val noteId: String,
        val loading: Boolean = true,
        val initialAttachmentIndex: Int = 0,
        val error: MediaGalleryError? = null,
        val attachments: List<NoteAttachmentUi> = emptyList(),
    ) {
        sealed class MediaGalleryError {
            data class FailedToSaveMedia(val cause: Throwable) : MediaGalleryError()
        }
    }

    sealed class UiEvent {
        data class SaveMedia(val remoteUrl: String) : UiEvent()
        data object DismissError : UiEvent()
    }

    sealed class SideEffect {
        data object MediaSaved : SideEffect()
    }
}

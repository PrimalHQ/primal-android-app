package net.primal.android.attachments.gallery

import android.graphics.Bitmap
import net.primal.android.attachments.domain.NoteAttachmentType
import net.primal.android.core.compose.attachment.model.NoteAttachmentUi

interface MediaGalleryContract {
    data class UiState(
        val noteId: String,
        val loading: Boolean = true,
        val initialAttachmentIndex: Int = 0,
        val initialPositionMs: Long = 0,
        val error: MediaGalleryError? = null,
        val attachments: List<NoteAttachmentUi> = emptyList(),
        val currentDisplayedBitmap: Bitmap? = null,
    ) {
        sealed class MediaGalleryError {
            data class FailedToSaveMedia(val cause: Throwable) : MediaGalleryError()
        }
    }

    sealed class UiEvent {
        data class SaveMedia(val attachment: NoteAttachmentUi) : UiEvent()
        data class LoadBitmap(val bitmap: Bitmap) : UiEvent()
        data object DismissError : UiEvent()
    }

    sealed class SideEffect {
        data class MediaSaved(val type: NoteAttachmentType) : SideEffect()
    }
}

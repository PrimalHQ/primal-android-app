package net.primal.android.attachments.gallery

import net.primal.android.core.compose.attachment.model.NoteAttachmentUi

interface MediaGalleryContract {
    data class UiState(
        val noteId: String,
        val loading: Boolean = true,
        val initialAttachmentIndex: Int = 0,
        val attachments: List<NoteAttachmentUi> = emptyList(),
    )
}

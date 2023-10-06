package net.primal.android.discuss.post

import android.net.Uri
import net.primal.android.feed.domain.NoteAttachment
import java.util.UUID

interface NewPostContract {

    data class UiState(
        val preFillContent: String? = null,
        val publishing: Boolean = false,
        val uploadingAttachments: Boolean = false,
        val error: NewPostError? = null,
        val activeAccountAvatarUrl: String? = null,
        val attachments: List<NoteAttachment> = emptyList(),
    ) {
        sealed class NewPostError {
            data class PublishError(val cause: Throwable?) : NewPostError()
            data class MissingRelaysConfiguration(val cause: Throwable) : NewPostError()
        }

    }

    sealed class UiEvent {
        data class PublishPost(val content: String) : UiEvent()
        data class ImportLocalFiles(val uris: List<Uri>) : UiEvent()
        data class DiscardNoteAttachment(val attachmentId: UUID) : UiEvent()
        data class RetryUpload(val attachmentId: UUID) : UiEvent()
    }

    sealed class SideEffect {
        data object PostPublished : SideEffect()
    }

}

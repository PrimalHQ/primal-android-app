package net.primal.android.editor

import android.net.Uri
import java.util.*
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.editor.domain.NoteAttachment

interface NoteEditorContract {

    data class UiState(
        val conversation: List<FeedPostUi> = emptyList(),
        val preFillContent: String? = null,
        val publishing: Boolean = false,
        val error: NewPostError? = null,
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val uploadingAttachments: Boolean = false,
        val attachments: List<NoteAttachment> = emptyList(),
    ) {
        val isReply: Boolean get() = conversation.isNotEmpty()
        val replyToNote: FeedPostUi? = conversation.lastOrNull()

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

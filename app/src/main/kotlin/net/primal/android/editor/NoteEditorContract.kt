package net.primal.android.editor

import android.net.Uri
import java.util.*
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.note.TaggedUser
import net.primal.android.core.compose.profile.model.UserProfileItemUi
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
        val userTaggingQuery: String? = null,
        val users: List<UserProfileItemUi> = emptyList(),
        val recommendedUsers: List<UserProfileItemUi> = emptyList(),
    ) {
        val isReply: Boolean get() = conversation.isNotEmpty()
        val replyToNote: FeedPostUi? = conversation.lastOrNull()

        sealed class NewPostError {
            data class PublishError(val cause: Throwable?) : NewPostError()
            data class MissingRelaysConfiguration(val cause: Throwable) : NewPostError()
        }
    }

    sealed class UiEvent {
        data class PublishPost(
            val content: String,
            val taggedUsers: List<TaggedUser>,
        ) : UiEvent()
        data class ImportLocalFiles(val uris: List<Uri>) : UiEvent()
        data class DiscardNoteAttachment(val attachmentId: UUID) : UiEvent()
        data class RetryUpload(val attachmentId: UUID) : UiEvent()
        data class SearchUsers(val query: String) : UiEvent()
        data class ToggleSearchUsers(val enabled: Boolean) : UiEvent()
    }

    sealed class SideEffect {
        data object PostPublished : SideEffect()
    }
}

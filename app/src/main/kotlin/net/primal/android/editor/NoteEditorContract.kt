package net.primal.android.editor

import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import java.util.*
import net.primal.android.articles.feed.ui.FeedArticleUi
import net.primal.android.articles.highlights.HighlightUi
import net.primal.android.core.compose.profile.model.UserProfileItemUi
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.editor.domain.NoteTaggedUser
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.domain.links.CdnImage

interface NoteEditorContract {

    data class UiState(
        val content: TextFieldValue = TextFieldValue(),
        val conversation: List<FeedPostUi> = emptyList(),
        val referencedArticle: FeedArticleUi? = null,
        val referencedHighlight: HighlightUi? = null,
        val isQuoting: Boolean = false,
        val publishing: Boolean = false,
        val error: NoteEditorError? = null,
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val activeAccountLegendaryCustomization: LegendaryCustomization? = null,
        val uploadingAttachments: Boolean = false,
        val attachments: List<NoteAttachment> = emptyList(),
        val taggedUsers: List<NoteTaggedUser> = emptyList(),
        val userTaggingQuery: String? = null,
        val users: List<UserProfileItemUi> = emptyList(),
        val recentUsers: List<UserProfileItemUi> = emptyList(),
        val popularUsers: List<UserProfileItemUi> = emptyList(),
    ) {
        val isReply: Boolean get() = conversation.isNotEmpty()
        val replyToNote: FeedPostUi? = conversation.lastOrNull()
        val recommendedUsers: List<UserProfileItemUi> get() = recentUsers + popularUsers
        sealed class NoteEditorError {
            data class PublishError(val cause: Throwable?) : NoteEditorError()
            data class MissingRelaysConfiguration(val cause: Throwable) : NoteEditorError()
        }
    }

    sealed class UiEvent {
        data class UpdateContent(val content: TextFieldValue) : UiEvent()
        data object AppendUserTagAtSign : UiEvent()
        data object PublishNote : UiEvent()
        data class ImportLocalFiles(val uris: List<Uri>) : UiEvent()
        data class DiscardNoteAttachment(val attachmentId: UUID) : UiEvent()
        data class RetryUpload(val attachmentId: UUID) : UiEvent()
        data class SearchUsers(val query: String) : UiEvent()
        data class ToggleSearchUsers(val enabled: Boolean) : UiEvent()
        data class TagUser(val taggedUser: NoteTaggedUser) : UiEvent()
    }

    sealed class SideEffect {
        data object PostPublished : SideEffect()
    }
}

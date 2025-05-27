package net.primal.android.editor

import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import java.util.*
import net.primal.android.articles.feed.ui.FeedArticleUi
import net.primal.android.articles.highlights.HighlightUi
import net.primal.android.core.compose.profile.model.UserProfileItemUi
import net.primal.android.core.errors.UiError
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.editor.domain.NoteTaggedUser
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.domain.links.CdnImage
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nevent

interface NoteEditorContract {

    data class UiState(
        val content: TextFieldValue = TextFieldValue(),
        val conversation: List<FeedPostUi> = emptyList(),
        val referencedArticle: FeedArticleUi? = null,
        val referencedHighlight: HighlightUi? = null,
        val isQuoting: Boolean = false,
        val publishing: Boolean = false,
        val error: UiError? = null,
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val activeAccountLegendaryCustomization: LegendaryCustomization? = null,
        val activeAccountBlossoms: List<String> = emptyList(),
        val uploadingAttachments: Boolean = false,
        val attachments: List<NoteAttachment> = emptyList(),
        val taggedUsers: List<NoteTaggedUser> = emptyList(),
        val userTaggingQuery: String? = null,
        val users: List<UserProfileItemUi> = emptyList(),
        val recentUsers: List<UserProfileItemUi> = emptyList(),
        val popularUsers: List<UserProfileItemUi> = emptyList(),
        val nostrUris: List<ReferencedUri<*>> = emptyList(),
    ) {
        val isReply: Boolean get() = conversation.isNotEmpty()
        val replyToNote: FeedPostUi? = conversation.lastOrNull()
        val recommendedUsers: List<UserProfileItemUi> get() = recentUsers + popularUsers
    }

    sealed class UiEvent {
        data class UpdateContent(val content: TextFieldValue) : UiEvent()
        data class PasteContent(val content: TextFieldValue) : UiEvent()
        data class RefreshUri(val uri: String) : UiEvent()
        data class RemoveUri(val uriIndex: Int) : UiEvent()
        data object AppendUserTagAtSign : UiEvent()
        data object PublishNote : UiEvent()
        data class ImportLocalFiles(val uris: List<Uri>) : UiEvent()
        data class DiscardNoteAttachment(val attachmentId: UUID) : UiEvent()
        data class RetryUpload(val attachmentId: UUID) : UiEvent()
        data class SearchUsers(val query: String) : UiEvent()
        data class ToggleSearchUsers(val enabled: Boolean) : UiEvent()
        data class TagUser(val taggedUser: NoteTaggedUser) : UiEvent()
        data object DismissError : UiEvent()
    }

    sealed class SideEffect {
        data object PostPublished : SideEffect()
    }

    sealed interface ReferencedUri<T> {
        val loading: Boolean
        val uri: String
        val data: T?

        data class Note(
            override val data: FeedPostUi?,
            override val loading: Boolean,
            override val uri: String,
            val nevent: Nevent,
        ) : ReferencedUri<FeedPostUi>

        data class Article(
            override val data: FeedArticleUi?,
            override val loading: Boolean,
            override val uri: String,
            val naddr: Naddr,
        ) : ReferencedUri<FeedArticleUi>
    }
}

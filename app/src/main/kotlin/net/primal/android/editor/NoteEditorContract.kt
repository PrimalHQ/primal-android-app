package net.primal.android.editor

import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import java.util.*
import net.primal.android.articles.feed.ui.FeedArticleUi
import net.primal.android.articles.highlights.HighlightUi
import net.primal.android.core.errors.UiError
import net.primal.android.drawer.multiaccount.model.UserAccountUi
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.editor.domain.NoteTaggedUser
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.profile.mention.UserTaggingState
import net.primal.domain.links.ReferencedStream
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nevent

interface NoteEditorContract {

    data class UiState(
        val content: TextFieldValue = TextFieldValue(),
        val replyToConversation: List<FeedPostUi> = emptyList(),
        val replyToArticle: FeedArticleUi? = null,
        val replyToHighlight: HighlightUi? = null,
        val isQuoting: Boolean = false,
        val publishing: Boolean = false,
        val error: UiError? = null,
        val selectedAccount: UserAccountUi? = null,
        val uploadingAttachments: Boolean = false,
        val attachments: List<NoteAttachment> = emptyList(),
        val taggedUsers: List<NoteTaggedUser> = emptyList(),
        val referencedNostrUris: List<ReferencedUri<*>> = emptyList(),
        val userTaggingState: UserTaggingState = UserTaggingState(),
        val availableAccounts: List<UserAccountUi> = emptyList(),
    ) {
        val isReply: Boolean get() = replyToConversation.isNotEmpty()
        val replyToNote: FeedPostUi? = replyToConversation.lastOrNull()
    }

    sealed class UiEvent {
        data class UpdateContent(val content: TextFieldValue) : UiEvent()
        data class PasteContent(val content: TextFieldValue) : UiEvent()
        data class RefreshUri(val uri: String) : UiEvent()
        data class RemoveUri(val uriIndex: Int) : UiEvent()
        data class RemoveHighlightByArticle(val articleATag: String) : UiEvent()
        data object AppendUserTagAtSign : UiEvent()
        data object PublishNote : UiEvent()
        data class ImportLocalFiles(val uris: List<Uri>) : UiEvent()
        data class DiscardNoteAttachment(val attachmentId: UUID) : UiEvent()
        data class RetryUpload(val attachmentId: UUID) : UiEvent()
        data class SearchUsers(val query: String) : UiEvent()
        data class ToggleSearchUsers(val enabled: Boolean) : UiEvent()
        data class TagUser(val taggedUser: NoteTaggedUser) : UiEvent()
        data class SelectAccount(val accountId: String) : UiEvent()
        data class InsertGifUrl(val url: String) : UiEvent()
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

        data class Highlight(
            override val data: HighlightUi?,
            override val loading: Boolean,
            override val uri: String,
            val nevent: Nevent,
        ) : ReferencedUri<HighlightUi>

        data class LightningInvoice(
            override val loading: Boolean,
            override val uri: String,
            override val data: String,
        ) : ReferencedUri<String>

        data class Stream(
            override val data: ReferencedStream?,
            override val loading: Boolean,
            override val uri: String,
            val naddr: Naddr,
        ) : ReferencedUri<ReferencedStream>
    }

    data class ScreenCallbacks(
        val onClose: () -> Unit,
        val onGifPickerClick: () -> Unit = {},
    )
}

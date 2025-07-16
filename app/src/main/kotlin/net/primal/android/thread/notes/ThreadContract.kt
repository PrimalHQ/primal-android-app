package net.primal.android.thread.notes

import androidx.compose.foundation.lazy.LazyListState
import net.primal.android.articles.feed.ui.FeedArticleUi
import net.primal.android.core.errors.UiError
import net.primal.android.editor.domain.NoteEditorArgs
import net.primal.android.notes.feed.model.FeedPostUi

interface ThreadContract {

    data class UiState(
        val activeAccountUserId: String,
        val highlightPostId: String,
        val highlightNote: FeedPostUi? = null,
        val highlightPostIndex: Int = 0,
        val conversation: List<FeedPostUi> = emptyList(),
        val fetching: Boolean = false,
        val replyToArticle: FeedArticleUi? = null,
        val error: UiError? = null,
        val listState: LazyListState = LazyListState(),
    )

    sealed class UiEvent {
        data object UpdateConversation : UiEvent()
        data object DismissError : UiEvent()
    }

    data class ScreenCallbacks(
        val onClose: () -> Unit,
        val onGoToWallet: () -> Unit,
        val onExpandReply: (args: NoteEditorArgs) -> Unit,
    )
}

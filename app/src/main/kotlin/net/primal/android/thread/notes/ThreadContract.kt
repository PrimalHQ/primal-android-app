package net.primal.android.thread.notes

import net.primal.android.articles.feed.ui.FeedArticleUi
import net.primal.android.note.ui.EventZapUiModel
import net.primal.android.notes.feed.model.FeedPostUi

interface ThreadContract {

    data class UiState(
        val highlightPostId: String,
        val highlightNote: FeedPostUi? = null,
        val highlightPostIndex: Int = 0,
        val conversation: List<FeedPostUi> = emptyList(),
        val fetching: Boolean = false,
        val topZaps: List<EventZapUiModel> = emptyList(),
        val replyToArticle: FeedArticleUi? = null,
    )

    sealed class UiEvent {
        data object UpdateConversation : UiEvent()
    }
}

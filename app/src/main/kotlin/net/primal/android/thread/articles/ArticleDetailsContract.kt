package net.primal.android.thread.articles

import net.primal.android.nostr.utils.Naddr
import net.primal.android.note.ui.EventZapUiModel
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.ZappingState
import net.primal.android.thread.articles.ui.ArticleDetailsUi

interface ArticleDetailsContract {
    data class UiState(
        val naddr: Naddr? = null,
        val article: ArticleDetailsUi? = null,
        val referencedNotes: List<FeedPostUi> = emptyList(),
        val npubToDisplayNameMap: Map<String, String> = emptyMap(),
        val topZaps: List<EventZapUiModel> = emptyList(),
        val comments: List<FeedPostUi> = emptyList(),
        val zappingState: ZappingState = ZappingState(),
        val shouldApproveBookmark: Boolean = false,
        val error: ArticleDetailsError? = null,
    )

    sealed class ArticleDetailsError {
        data object InvalidNaddr : ArticleDetailsError()
        data class MissingLightningAddress(val cause: Throwable) : ArticleDetailsError()
        data class InvalidZapRequest(val cause: Throwable) : ArticleDetailsError()
        data class FailedToPublishZapEvent(val cause: Throwable) : ArticleDetailsError()
        data class FailedToPublishRepostEvent(val cause: Throwable) : ArticleDetailsError()
        data class FailedToPublishLikeEvent(val cause: Throwable) : ArticleDetailsError()
        data class MissingRelaysConfiguration(val cause: Throwable) : ArticleDetailsError()
    }

    sealed class ArticlePartRender {
        data class MarkdownRender(val markdown: String) : ArticlePartRender()
        data class HtmlRender(val html: String) : ArticlePartRender()
        data class NoteRender(val note: FeedPostUi) : ArticlePartRender()
    }

    sealed class UiEvent {
        data object UpdateContent : UiEvent()
        data object DismissErrors : UiEvent()
        data class ZapArticle(val zapAmount: ULong? = null, val zapDescription: String? = null) : UiEvent()
        data object LikeArticle : UiEvent()
        data object RepostAction : UiEvent()
        data class BookmarkAction(val forceUpdate: Boolean = false) : UiEvent()
        data object DismissBookmarkConfirmation : UiEvent()
    }
}

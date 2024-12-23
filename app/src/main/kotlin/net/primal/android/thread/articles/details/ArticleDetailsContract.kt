package net.primal.android.thread.articles.details

import net.primal.android.core.errors.UiError
import net.primal.android.highlights.model.JoinedHighlightsUi
import net.primal.android.nostr.utils.Naddr
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.ZappingState
import net.primal.android.stats.ui.EventZapUiModel
import net.primal.android.thread.articles.details.ui.ArticleDetailsUi

interface ArticleDetailsContract {
    data class UiState(
        val naddr: Naddr? = null,
        val showHighlights: Boolean = true,
        val isAuthorFollowed: Boolean = false,
        val article: ArticleDetailsUi? = null,
        val highlights: List<JoinedHighlightsUi> = emptyList(),
        val referencedNotes: List<FeedPostUi> = emptyList(),
        val npubToDisplayNameMap: Map<String, String> = emptyMap(),
        val topZaps: List<EventZapUiModel> = emptyList(),
        val comments: List<FeedPostUi> = emptyList(),
        val zappingState: ZappingState = ZappingState(),
        val selectedHighlight: JoinedHighlightsUi? = null,
        val isHighlighted: Boolean = false,
        val error: UiError? = null,
        val isWorking: Boolean = false,
    )

    sealed class ArticlePartRender {
        data class MarkdownRender(val markdown: String) : ArticlePartRender()
        data class NoteRender(val note: FeedPostUi) : ArticlePartRender()
    }

    sealed class UiEvent {
        data object UpdateContent : UiEvent()
        data object DismissErrors : UiEvent()
        data class ZapArticle(val zapAmount: ULong? = null, val zapDescription: String? = null) : UiEvent()
        data object LikeArticle : UiEvent()
        data object RepostAction : UiEvent()
        data object ToggleAuthorFollows : UiEvent()
        data object ToggleHighlights : UiEvent()
        data class SelectHighlight(val content: String) : UiEvent()
        data object DismissSelectedHighlight : UiEvent()
        data object PublishSelectedHighlight : UiEvent()
        data object DeleteSelectedHighlight : UiEvent()
    }
}

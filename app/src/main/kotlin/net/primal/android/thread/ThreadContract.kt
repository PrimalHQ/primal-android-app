package net.primal.android.thread

import net.primal.android.core.compose.feed.model.FeedPostUi

interface ThreadContract {

    data class UiState(
        val conversation: List<FeedPostUi> = emptyList(),
        val highlightPostIndex: Int = 0,
    )

    sealed class UiEvent {
        data class PostLikeAction(val postId: String, val postAuthorId: String) : UiEvent()
    }
}

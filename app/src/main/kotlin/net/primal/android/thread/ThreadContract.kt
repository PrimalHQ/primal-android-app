package net.primal.android.thread

import net.primal.android.core.compose.feed.model.FeedPostUi

interface ThreadContract {

    data class UiState(
        val publishingReply: Boolean = false,
        val publishingError: PublishError? = null,
        val conversation: List<FeedPostUi> = emptyList(),
        val highlightPostIndex: Int = 0,
    ) {
        data class PublishError(val cause: Throwable?)
    }

    sealed class UiEvent {
        data class PostLikeAction(val postId: String, val postAuthorId: String) : UiEvent()
        data class RepostAction(
            val postId: String,
            val postAuthorId: String,
            val postNostrEvent: String
        ) : UiEvent()

        data class ReplyToAction(
            val content: String,
            val replyToPostId: String,
            val replyToAuthorId: String,
        ) : UiEvent()
    }
}

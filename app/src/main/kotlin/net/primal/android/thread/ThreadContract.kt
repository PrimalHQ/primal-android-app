package net.primal.android.thread

import net.primal.android.core.compose.feed.model.FeedPostUi

interface ThreadContract {

    data class UiState(
        val replyText: String = "",
        val publishingReply: Boolean = false,
        val conversation: List<FeedPostUi> = emptyList(),
        val highlightPostId: String? = null,
        val highlightPostIndex: Int = 0,
        val walletConnected: Boolean = false,
        val error: PostActionError? = null,
    )

    sealed class UiEvent {
        data class PostLikeAction(val postId: String, val postAuthorId: String) : UiEvent()
        data class UpdateReply(val newReply: String) : UiEvent()
        data class RepostAction(
            val postId: String,
            val postAuthorId: String,
            val postNostrEvent: String
        ) : UiEvent()
        data class ZapAction(
            val postId: String,
            val postAuthorId: String,
            val postAuthorLightningAddress: String?,
            val zapAmount: Int?,
            val zapDescription: String?,

        ) : UiEvent()
        data class ReplyToAction(
            val rootPostId: String,
            val replyToPostId: String,
            val replyToAuthorId: String,
        ) : UiEvent()
    }

    sealed class PostActionError {
        data object MissingLightningAddress : PostActionError()
        data object MalformedLightningAddress : PostActionError()
        data object FailedToPublishZapEvent : PostActionError()
        data object FailedToPublishRepostEvent : PostActionError()
        data object FailedToPublishReplyEvent : PostActionError()
        data object FailedToPublishLikeEvent : PostActionError()
    }
}

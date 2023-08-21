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
        val error: ThreadError? = null,
    ) {
        sealed class ThreadError {
            data class MissingLightningAddress(val cause: Throwable) : ThreadError()
            data class InvalidZapRequest(val cause: Throwable) : ThreadError()
            data class FailedToPublishZapEvent(val cause: Throwable) : ThreadError()
            data class FailedToPublishRepostEvent(val cause: Throwable) : ThreadError()
            data class FailedToPublishReplyEvent(val cause: Throwable) : ThreadError()
            data class FailedToPublishLikeEvent(val cause: Throwable) : ThreadError()
        }
    }

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
}

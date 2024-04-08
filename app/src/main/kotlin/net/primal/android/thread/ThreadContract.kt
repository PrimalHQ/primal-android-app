package net.primal.android.thread

import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.model.ZappingState
import net.primal.android.profile.report.ReportType

interface ThreadContract {

    data class UiState(
        val highlightPostId: String,
        val replyText: String = "",
        val publishingReply: Boolean = false,
        val conversation: List<FeedPostUi> = emptyList(),
        val highlightPostIndex: Int = 0,
        val zappingState: ZappingState = ZappingState(),
        val fetching: Boolean = false,
        val confirmBookmarkingNoteId: String? = null,
        val error: ThreadError? = null,
    ) {
        sealed class ThreadError {
            data class MissingLightningAddress(val cause: Throwable) : ThreadError()
            data class InvalidZapRequest(val cause: Throwable) : ThreadError()
            data class FailedToPublishZapEvent(val cause: Throwable) : ThreadError()
            data class FailedToPublishRepostEvent(val cause: Throwable) : ThreadError()
            data class FailedToPublishReplyEvent(val cause: Throwable) : ThreadError()
            data class FailedToPublishLikeEvent(val cause: Throwable) : ThreadError()
            data class MissingRelaysConfiguration(val cause: Throwable) : ThreadError()
            data class FailedToMuteUser(val cause: Throwable) : ThreadError()
        }
    }

    sealed class UiEvent {
        data object UpdateConversation : UiEvent()
        data class PostLikeAction(val postId: String, val postAuthorId: String) : UiEvent()
        data class UpdateReply(val newReply: String) : UiEvent()
        data class RepostAction(
            val postId: String,
            val postAuthorId: String,
            val postNostrEvent: String,
        ) : UiEvent()
        data class ZapAction(
            val postId: String,
            val postAuthorId: String,
            val zapAmount: ULong?,
            val zapDescription: String?,

        ) : UiEvent()
        data class ReplyToAction(
            val rootPostId: String,
            val replyToPostId: String,
            val replyToAuthorId: String,
        ) : UiEvent()
        data class MuteAction(val postAuthorId: String) : UiEvent()
        data class ReportAbuse(
            val reportType: ReportType,
            val profileId: String,
            val noteId: String,
        ) : UiEvent()
        data class BookmarkAction(val noteId: String, val forceUpdate: Boolean = false) : UiEvent()
        data object DismissBookmarkConfirmation : UiEvent()
    }
}

package net.primal.android.thread.notes

import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.model.ZappingState
import net.primal.android.note.ui.NoteZapUiModel
import net.primal.android.profile.report.ReportType

interface ThreadContract {

    data class UiState(
        val highlightPostId: String,
        val highlightNote: FeedPostUi? = null,
        val highlightPostIndex: Int = 0,
        val conversation: List<FeedPostUi> = emptyList(),
        val zappingState: ZappingState = ZappingState(),
        val fetching: Boolean = false,
        val confirmBookmarkingNoteId: String? = null,
        val topZap: NoteZapUiModel? = null,
        val otherZaps: List<NoteZapUiModel> = emptyList(),
        val error: ThreadError? = null,
    ) {
        sealed class ThreadError {
            data class MissingLightningAddress(val cause: Throwable) : ThreadError()
            data class InvalidZapRequest(val cause: Throwable) : ThreadError()
            data class FailedToPublishZapEvent(val cause: Throwable) : ThreadError()
            data class FailedToPublishRepostEvent(val cause: Throwable) : ThreadError()
            data class FailedToPublishLikeEvent(val cause: Throwable) : ThreadError()
            data class MissingRelaysConfiguration(val cause: Throwable) : ThreadError()
            data class FailedToMuteUser(val cause: Throwable) : ThreadError()
        }
    }

    sealed class UiEvent {
        data object UpdateConversation : UiEvent()

        data class PostLikeAction(val postId: String, val postAuthorId: String) : UiEvent()

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

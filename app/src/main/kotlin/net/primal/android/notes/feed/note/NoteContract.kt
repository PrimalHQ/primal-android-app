package net.primal.android.notes.feed.note

import net.primal.android.notes.feed.model.ZappingState
import net.primal.android.profile.report.ReportType

interface NoteContract {

    data class UiState(
        val zappingState: ZappingState = ZappingState(),
        val shouldApproveBookmark: Boolean = false,
    )

    sealed class NoteError {
        data class MissingLightningAddress(val cause: Throwable) : NoteError()
        data class InvalidZapRequest(val cause: Throwable) : NoteError()
        data class FailedToPublishZapEvent(val cause: Throwable) : NoteError()
        data class FailedToPublishRepostEvent(val cause: Throwable) : NoteError()
        data class FailedToPublishLikeEvent(val cause: Throwable) : NoteError()
        data class MissingRelaysConfiguration(val cause: Throwable) : NoteError()
        data class FailedToMuteUser(val cause: Throwable) : NoteError()
    }

    sealed class UiEvent {
        data class PostLikeAction(
            val postId: String,
            val postAuthorId: String,
        ) : UiEvent()

        data class RepostAction(
            val postId: String,
            val postAuthorId: String,
            val postNostrEvent: String,
        ) : UiEvent()

        data class ZapAction(
            val postId: String,
            val postAuthorId: String,
            val zapAmount: ULong? = null,
            val zapDescription: String? = null,
        ) : UiEvent()

        data class MuteAction(
            val userId: String,
        ) : UiEvent()

        data class ReportAbuse(
            val reportType: ReportType,
            val profileId: String,
            val noteId: String,
        ) : UiEvent()

        data class BookmarkAction(
            val noteId: String,
            val forceUpdate: Boolean = false,
        ) : UiEvent()

        data class DismissBookmarkConfirmation(
            val noteId: String,
        ) : UiEvent()
    }
}

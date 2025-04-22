package net.primal.android.notes.feed.note

import net.primal.android.core.errors.UiError
import net.primal.android.notes.feed.model.ZappingState
import net.primal.domain.nostr.ReportType

interface NoteContract {

    data class UiState(
        val zappingState: ZappingState = ZappingState(),
        val shouldApproveBookmark: Boolean = false,
        val relayHints: List<String> = emptyList(),
        val error: UiError? = null,
    )

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

        data class MuteUserAction(
            val userId: String,
        ) : UiEvent()

        data class MuteThreadAction(
            val postId: String,
        ) : UiEvent()

        data class UnmuteThreadAction(
            val postId: String,
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

        data object DismissError : UiEvent()
    }
}

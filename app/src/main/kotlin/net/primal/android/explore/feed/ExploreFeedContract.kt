package net.primal.android.explore.feed

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.model.ZappingState
import net.primal.android.profile.report.ReportType

interface ExploreFeedContract {
    data class UiState(
        val feedDirective: String,
        val existsInUserFeeds: Boolean = false,
        val canBeAddedInUserFeeds: Boolean = true,
        val zappingState: ZappingState = ZappingState(),
        val posts: Flow<PagingData<FeedPostUi>>,
        val confirmBookmarkingNoteId: String? = null,
        val error: ExploreFeedError? = null,
    ) {
        sealed class ExploreFeedError {
            data class MissingLightningAddress(val cause: Throwable) : ExploreFeedError()
            data class InvalidZapRequest(val cause: Throwable) : ExploreFeedError()
            data class FailedToPublishZapEvent(val cause: Throwable) : ExploreFeedError()
            data class FailedToPublishRepostEvent(val cause: Throwable) : ExploreFeedError()
            data class FailedToPublishLikeEvent(val cause: Throwable) : ExploreFeedError()
            data class MissingRelaysConfiguration(val cause: Throwable) : ExploreFeedError()
            data class FailedToAddToFeed(val cause: Throwable) : ExploreFeedError()
            data class FailedToRemoveFeed(val cause: Throwable) : ExploreFeedError()
            data class FailedToMuteUser(val cause: Throwable) : ExploreFeedError()
        }
    }

    sealed class UiEvent {
        data class AddToUserFeeds(val title: String) : UiEvent()
        data object RemoveFromUserFeeds : UiEvent()
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
        data class MuteAction(val profileId: String) : UiEvent()
        data class ReportAbuse(
            val reportType: ReportType,
            val profileId: String,
            val noteId: String,
        ) : UiEvent()
        data class BookmarkAction(val noteId: String, val forceUpdate: Boolean = false) : UiEvent()
        data object DismissBookmarkConfirmation : UiEvent()
    }
}

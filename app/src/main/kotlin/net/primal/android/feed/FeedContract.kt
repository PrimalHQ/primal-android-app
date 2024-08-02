package net.primal.android.feed

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.feed.list.FeedUi
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.model.FeedPostsSyncStats
import net.primal.android.core.compose.feed.model.ZappingState
import net.primal.android.profile.report.ReportType
import net.primal.android.user.domain.Badges

interface FeedContract {
    data class UiState(
        val posts: Flow<PagingData<FeedPostUi>>,
        val feeds: List<FeedUi> = emptyList(),
        val feedPostsCount: Int = 0,
        val feedTitle: String = "",
        val feedAutoRefresh: Boolean = false,
        val activeAccountAvatarCdnImage: CdnImage? = null,
        val zappingState: ZappingState = ZappingState(),
        val syncStats: FeedPostsSyncStats = FeedPostsSyncStats(),
        val badges: Badges = Badges(),
        val confirmBookmarkingNoteId: String? = null,
        val topVisibleNote: Pair<String, String?>? = null,
        val error: FeedError? = null,
    ) {
        sealed class FeedError {
            data class MissingLightningAddress(val cause: Throwable) : FeedError()
            data class InvalidZapRequest(val cause: Throwable) : FeedError()
            data class FailedToPublishZapEvent(val cause: Throwable) : FeedError()
            data class FailedToPublishRepostEvent(val cause: Throwable) : FeedError()
            data class FailedToPublishLikeEvent(val cause: Throwable) : FeedError()
            data class MissingRelaysConfiguration(val cause: Throwable) : FeedError()
            data class FailedToMuteUser(val cause: Throwable) : FeedError()
        }
    }

    sealed class UiEvent {
        data object FeedScrolledToTop : UiEvent()
        data object RequestUserDataUpdate : UiEvent()
        data object StartPolling : UiEvent()
        data object StopPolling : UiEvent()
        data object ShowLatestNotes : UiEvent()
        data class UpdateCurrentTopVisibleNote(val noteId: String, val repostId: String? = null) : UiEvent()
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

        data class MuteAction(val userId: String) : UiEvent()
        data class ReportAbuse(
            val reportType: ReportType,
            val profileId: String,
            val noteId: String,
        ) : UiEvent()

        data class BookmarkAction(val noteId: String, val forceUpdate: Boolean = false) : UiEvent()
        data object DismissBookmarkConfirmation : UiEvent()
    }
}

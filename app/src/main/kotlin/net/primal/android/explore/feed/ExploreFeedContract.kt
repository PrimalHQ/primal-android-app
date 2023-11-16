package net.primal.android.explore.feed

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.core.compose.feed.model.FeedPostUi

interface ExploreFeedContract {
    data class UiState(
        val title: String,
        val existsInUserFeeds: Boolean = false,
        val walletConnected: Boolean = false,
        val defaultZapAmount: ULong? = null,
        val zapOptions: List<ULong> = emptyList(),
        val posts: Flow<PagingData<FeedPostUi>>,
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
        data object AddToUserFeeds : UiEvent()
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
    }
}

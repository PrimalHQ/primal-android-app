package net.primal.android.explore.feed

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.core.compose.feed.model.FeedPostUi

interface ExploreFeedContract {
    data class UiState(
        val title: String,
        val existsInUserFeeds: Boolean = false,
        val walletConnected: Boolean = false,
        val posts: Flow<PagingData<FeedPostUi>>,
        val error: PostActionError? = null,
    ) {
        sealed class PostActionError {
            data class MissingLightningAddress(val cause: Throwable) : PostActionError()
            data class MalformedLightningAddress(val cause: Throwable) : PostActionError()
            data class FailedToPublishZapEvent(val cause: Throwable) : PostActionError()
            data class FailedToPublishRepostEvent(val cause: Throwable) : PostActionError()
            data class FailedToPublishLikeEvent(val cause: Throwable) : PostActionError()
        }
    }

    sealed class UiEvent {
        data object AddToUserFeeds : UiEvent()
        data object RemoveFromUserFeeds : UiEvent()
        data class PostLikeAction(val postId: String, val postAuthorId: String) : UiEvent()
        data class RepostAction(
            val postId: String,
            val postAuthorId: String,
            val postNostrEvent: String
        ) : UiEvent()
        data class ZapAction(
            val postId: String,
            val postAuthorId: String,
            val zapAmount: Int?,
            val zapDescription: String?,
            val postAuthorLightningAddress: String?
        ) : UiEvent()
    }
}

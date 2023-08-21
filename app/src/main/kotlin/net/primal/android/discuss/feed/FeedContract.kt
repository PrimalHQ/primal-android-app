package net.primal.android.discuss.feed

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.model.FeedPostsSyncStats

interface FeedContract {
    data class UiState(
        val feedPostsCount: Int = 0,
        val feedTitle: String = "",
        val activeAccountAvatarUrl: String? = null,
        val walletConnected: Boolean = false,
        val posts: Flow<PagingData<FeedPostUi>>,
        val syncStats: FeedPostsSyncStats = FeedPostsSyncStats(),
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
        data object FeedScrolledToTop : UiEvent()
        data object RequestUserDataUpdate : UiEvent()
        data class PostLikeAction(val postId: String, val postAuthorId: String) : UiEvent()
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
    }

}

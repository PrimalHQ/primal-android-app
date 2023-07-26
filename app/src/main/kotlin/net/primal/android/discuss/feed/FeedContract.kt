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
        val posts: Flow<PagingData<FeedPostUi>>,
        val syncStats: FeedPostsSyncStats = FeedPostsSyncStats(),
    )

    sealed class UiEvent {
        data object FeedScrolledToTop : UiEvent()
        data object RequestSyncSettings : UiEvent()
        data class PostLikeAction(val postId: String, val postAuthorId: String) : UiEvent()
        data class RepostAction(
            val postId: String,
            val postAuthorId: String,
            val postNostrEvent: String
        ) : UiEvent()
    }

}

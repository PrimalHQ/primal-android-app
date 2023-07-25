package net.primal.android.explore.feed

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.core.compose.feed.model.FeedPostUi

interface ExploreFeedContract {
    data class UiState(
        val title: String,
        val existsInUserFeeds: Boolean = false,
        val posts: Flow<PagingData<FeedPostUi>>,
    )

    sealed class UiEvent {
        object AddToUserFeeds : UiEvent()
        object RemoveFromUserFeeds : UiEvent()
        data class PostLikeAction(val postId: String, val postAuthorId: String) : UiEvent()
        data class RepostAction(
            val postId: String,
            val postAuthorId: String,
            val postNostrEvent: String
        ) : UiEvent()
    }
}

package net.primal.android.feed.feed

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.feed.shared.model.FeedPostUi
import net.primal.android.feed.shared.model.FeedPostsSyncStats

interface FeedContract {
    data class UiState(
        val feedPostsCount: Int = 0,
        val feedTitle: String = "",
        val posts: Flow<PagingData<FeedPostUi>>,
        val syncStats: FeedPostsSyncStats = FeedPostsSyncStats(),
    )

    sealed class UiEvent {
        object FeedScrolledToTop : UiEvent()
    }

}
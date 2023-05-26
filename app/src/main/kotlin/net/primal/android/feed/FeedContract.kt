package net.primal.android.feed

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.feed.ui.model.FeedPostUi

interface FeedContract {
    data class UiState(
        val feedPostsCount: Int = 0,
        val feedTitle: String = "",
        val posts: Flow<PagingData<FeedPostUi>>,
    )

    sealed class UiEvent {

    }

    sealed class SideEffect {

    }

}
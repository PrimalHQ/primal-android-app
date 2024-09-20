package net.primal.android.explore.feed.note

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.notes.feed.model.FeedPostUi

interface ExploreNoteFeedContract {
    data class UiState(
        val feedSpec: String,
        val renderType: RenderType,
        val existsInUserFeeds: Boolean = false,
        val canBeAddedInUserFeeds: Boolean = true,
        val notes: Flow<PagingData<FeedPostUi>>,
        val error: ExploreFeedError? = null,
    ) {
        sealed class ExploreFeedError {
            data class FailedToAddToFeed(val cause: Throwable) : ExploreFeedError()
            data class FailedToRemoveFeed(val cause: Throwable) : ExploreFeedError()
        }
    }

    sealed class UiEvent {
        data class AddToUserFeeds(val title: String) : UiEvent()
        data object RemoveFromUserFeeds : UiEvent()
    }

    enum class RenderType {
        List, Grid;

        fun isList() = this == List
        fun isGrid() = this == Grid
    }
}

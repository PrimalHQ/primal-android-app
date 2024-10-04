package net.primal.android.explore.feed

import net.primal.android.feeds.domain.FeedSpecKind

interface ExploreFeedContract {
    data class UiState(
        val feedSpec: String,
        val feedSpecKind: FeedSpecKind?,
        val renderType: RenderType,
        val existsInUserFeeds: Boolean = false,
        val canBeAddedInUserFeeds: Boolean = true,
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
        List,
        Grid,
        ;

        fun isList() = this == List
        fun isGrid() = this == Grid
    }
}

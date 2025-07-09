package net.primal.android.explore.feed

import net.primal.domain.feeds.FeedSpecKind

interface ExploreFeedContract {
    data class UiState(
        val feedSpec: String,
        val feedSpecKind: FeedSpecKind?,
        val renderType: RenderType,
        val feedTitle: String?,
        val feedDescription: String?,
        val existsInUserFeeds: Boolean = false,
        val error: ExploreFeedError? = null,
    ) {
        sealed class ExploreFeedError {
            data class FailedToAddToFeed(val cause: Throwable) : ExploreFeedError()
            data class FailedToRemoveFeed(val cause: Throwable) : ExploreFeedError()
        }
    }

    sealed class UiEvent {
        data class AddToUserFeeds(val title: String, val description: String) : UiEvent()
        data object RemoveFromUserFeeds : UiEvent()
    }

    enum class RenderType {
        List,
        Grid,
        ;

        fun isList() = this == List
        fun isGrid() = this == Grid
    }

    data class ScreenCallbacks(
        val onClose: () -> Unit,
        val onGoToWallet: () -> Unit,
    )
}

package net.primal.android.explore.home.feeds

import net.primal.android.feeds.domain.DvmFeed
import net.primal.android.feeds.ui.model.FeedUi

interface ExploreFeedsContract {
    data class UiState(
        val feeds: List<DvmFeed> = emptyList(),
        val userReadFeeds: List<FeedUi> = emptyList(),
        val userNoteFeeds: List<FeedUi> = emptyList(),
        val loading: Boolean = true,
        val error: Throwable? = null,
    )

    sealed class UiEvent {
        data class AddToUserFeeds(val dvmFeed: DvmFeed) : UiEvent()
        data class RemoveFromUserFeeds(val dvmFeed: DvmFeed) : UiEvent()

        data object RefreshFeeds : UiEvent()
    }
}

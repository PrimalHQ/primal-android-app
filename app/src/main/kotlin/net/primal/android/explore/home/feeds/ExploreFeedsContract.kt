package net.primal.android.explore.home.feeds

import net.primal.android.feeds.domain.DvmFeed

interface ExploreFeedsContract {
    data class UiState(
        val feeds: List<DvmFeed> = emptyList(),
        val userFeedSpecs: List<String> = emptyList(),
        val loading: Boolean = true,
    )

    sealed class UiEvent {
        data class AddToUserFeeds(val dvmFeed: DvmFeed) : UiEvent()
        data class RemoveFromUserFeeds(val dvmFeed: DvmFeed) : UiEvent()

        data object RefreshFeeds : UiEvent()
    }
}

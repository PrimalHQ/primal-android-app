package net.primal.android.explore.home.feeds

import net.primal.android.feeds.dvm.ui.DvmFeedUi

interface ExploreFeedsContract {
    data class UiState(
        val feeds: List<DvmFeedUi> = emptyList(),
        val userFeedSpecs: List<String> = emptyList(),
        val loading: Boolean = true,
    )

    sealed class UiEvent {
        data class AddToUserFeeds(val dvmFeed: DvmFeedUi) : UiEvent()
        data class RemoveFromUserFeeds(val dvmFeed: DvmFeedUi) : UiEvent()

        data object RefreshFeeds : UiEvent()
    }
}

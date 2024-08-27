package net.primal.android.feeds

import net.primal.android.feeds.repository.DvmFeed
import net.primal.android.feeds.ui.model.FeedUi

interface ReadsFeedsContract {
    data class UiState(
        val activeFeed: FeedUi,
        val feeds: List<FeedUi> = emptyList(),
        val feedMarketplaceStage: FeedMarketplaceStage = FeedMarketplaceStage.FeedList,
        val fetchingDvmFeeds: Boolean = false,
        val dvmFeeds: List<DvmFeed> = emptyList(),
        val selectedDvmFeed: DvmFeed? = null,
    ) {
        enum class FeedMarketplaceStage {
            FeedList,
            FeedMarketplace,
            FeedDetails,
        }
    }

    sealed class UiEvent {
        data object ShowFeedMarketplace : UiEvent()
        data object CloseFeedMarketplace : UiEvent()
        data class ShowFeedDetails(val dvmFeed: DvmFeed) : UiEvent()
        data object CloseFeedDetails : UiEvent()
        data class AddDvmFeedToUserFeeds(val dvmFeed: DvmFeed) : UiEvent()
        data class RemoveDvmFeedFromUserFeeds(val dvmFeed: DvmFeed) : UiEvent()
    }
}

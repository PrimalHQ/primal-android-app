package net.primal.android.feeds

import net.primal.android.feeds.domain.DvmFeed
import net.primal.android.feeds.domain.FeedSpecKind
import net.primal.android.feeds.ui.model.FeedUi
import net.primal.android.notes.home.HomeFeedContract.UiEvent

interface FeedsContract {
    data class UiState(
        val activeFeed: FeedUi,
        val specKind: FeedSpecKind,
        val feeds: List<FeedUi> = emptyList(),
        val defaultFeeds: List<FeedUi> = emptyList(),
        val isEditMode: Boolean = false,
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
        data object OpenEditMode : UiEvent()
        data object CloseEditMode : UiEvent()
        data class UpdateFeedSpecEnabled(val feedSpec: String, val enabled: Boolean) : UiEvent()
        data class FeedReordered(val feeds: List<FeedUi>) : UiEvent()
        data object ShowFeedMarketplace : UiEvent()

        data object CloseFeedMarketplace : UiEvent()
        data class ShowFeedDetails(val dvmFeed: DvmFeed) : UiEvent()

        data object CloseFeedDetails : UiEvent()
        data class AddDvmFeedToUserFeeds(val dvmFeed: DvmFeed) : UiEvent()
        data class RemoveDvmFeedFromUserFeeds(val dvmFeed: DvmFeed) : UiEvent()
        data class RemoveFeedFromUserFeeds(val spec: String) : UiEvent()

        data object RestoreDefaultPrimalFeeds : UiEvent()
    }
}

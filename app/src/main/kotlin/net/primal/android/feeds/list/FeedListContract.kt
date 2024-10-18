package net.primal.android.feeds.list

import net.primal.android.feeds.domain.FeedSpecKind
import net.primal.android.feeds.dvm.ui.DvmFeedUi
import net.primal.android.feeds.list.ui.model.FeedUi

interface FeedListContract {
    data class UiState(
        val activeFeed: FeedUi,
        val specKind: FeedSpecKind,
        val feeds: List<FeedUi> = emptyList(),
        val isEditMode: Boolean = false,
        val feedMarketplaceStage: FeedMarketplaceStage = FeedMarketplaceStage.FeedList,
        val fetchingDvmFeeds: Boolean = false,
        val dvmFeeds: List<DvmFeedUi> = emptyList(),
        val selectedDvmFeed: DvmFeedUi? = null,
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
        data class ShowFeedDetails(val dvmFeed: DvmFeedUi) : UiEvent()

        data object CloseFeedDetails : UiEvent()
        data class AddDvmFeedToUserFeeds(val dvmFeed: DvmFeedUi) : UiEvent()
        data class RemoveDvmFeedFromUserFeeds(val dvmFeed: DvmFeedUi) : UiEvent()
        data class RemoveFeedFromUserFeeds(val spec: String) : UiEvent()

        data object RestoreDefaultPrimalFeeds : UiEvent()
    }
}

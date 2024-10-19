package net.primal.android.feeds.dvm

import net.primal.android.core.errors.UiError
import net.primal.android.feeds.dvm.ui.DvmFeedUi
import net.primal.android.notes.feed.model.ZappingState

interface DvmFeedListItemContract {

    data class UiState(
        val zappingState: ZappingState = ZappingState(),
        val error: UiError? = null,
    )

    sealed class UiEvent {
        data class OnLikeClick(val dvmFeed: DvmFeedUi) : UiEvent()
        data class OnZapClick(
            val dvmFeed: DvmFeedUi,
            val zapDescription: String? = null,
            val zapAmount: ULong? = null,
        ) : UiEvent()
        data object DismissError : UiEvent()
    }
}

package net.primal.android.feeds.item

import net.primal.android.core.errors.UiError
import net.primal.android.feeds.domain.DvmFeed
import net.primal.android.notes.feed.model.ZappingState

interface DvmFeedListItemContract {

    data class UiState(
        val zappingState: ZappingState = ZappingState(),
        val error: UiError? = null,
    )

    sealed class UiEvent {
        data class OnLikeClick(val dvmFeed: DvmFeed) : UiEvent()
        data class OnZapClick(
            val dvmFeed: DvmFeed,
            val zapDescription: String? = null,
            val zapAmount: ULong? = null,
        ) : UiEvent()
        data object DismissError : UiEvent()
    }
}

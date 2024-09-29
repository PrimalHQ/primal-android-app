package net.primal.android.feeds.item

import net.primal.android.feeds.domain.DvmFeed

interface DvmFeedListItemContract {
    data class UiState(
        val dvmFeed: DvmFeed,
    )

    sealed class UiEvent {
        data object OnLikeClick : UiEvent()
        data object OnZapClick : UiEvent()
    }
}

package net.primal.android.feeds.item

import net.primal.android.feeds.domain.DvmFeed

interface DvmFeedListItemContract {

    sealed class UiEvent {
        data class OnLikeClick(val dvmFeed: DvmFeed) : UiEvent()
        data class OnZapClick(val dvmFeed: DvmFeed) : UiEvent()
    }
}

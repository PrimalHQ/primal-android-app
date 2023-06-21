package net.primal.android.feed.thread

import net.primal.android.feed.shared.model.FeedPostUi

interface ThreadContract {

    data class UiState(
        val conversation: List<FeedPostUi> = emptyList(),
        val highlightPostIndex: Int = 0,
    )

}

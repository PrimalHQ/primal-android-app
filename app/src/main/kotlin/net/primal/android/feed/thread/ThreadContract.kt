package net.primal.android.feed.thread

import net.primal.android.feed.shared.model.FeedPostUi

interface ThreadContract {

    data class UiState(
        val highlightPost: FeedPostUi? = null,
        val highlightPostIndex: Int = 0,
        val conversation: List<FeedPostUi> = emptyList(),
    )

}

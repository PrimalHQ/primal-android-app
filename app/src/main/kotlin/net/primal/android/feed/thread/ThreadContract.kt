package net.primal.android.feed.thread

import net.primal.android.feed.shared.model.FeedPostUi

interface ThreadContract {

    data class UiState(
        val loading: Boolean,
        val rootPost: FeedPostUi? = null,
        val precedingReplies: List<FeedPostUi> = emptyList(),
        val succeedingReplies: List<FeedPostUi> = emptyList(),
    )

}

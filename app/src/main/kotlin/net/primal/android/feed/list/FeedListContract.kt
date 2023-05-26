package net.primal.android.feed.list

import net.primal.android.feed.ui.model.FeedUi

interface FeedListContract {

    data class UiState(
        val feeds: List<FeedUi> = emptyList(),
    )

}
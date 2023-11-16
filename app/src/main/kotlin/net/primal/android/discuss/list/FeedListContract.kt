package net.primal.android.discuss.list

import net.primal.android.discuss.list.model.FeedUi

interface FeedListContract {

    data class UiState(
        val feeds: List<FeedUi> = emptyList(),
    )
}

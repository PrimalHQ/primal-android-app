package net.primal.android.main.reads

import net.primal.android.feeds.list.ui.model.FeedUi

interface ReadsScreenContract {
    data class UiState(
        val feeds: List<FeedUi> = emptyList(),
        val loading: Boolean = false,
    )

    sealed class UiEvent {
        data object RestoreDefaultFeeds : UiEvent()
        data object RefreshReadsFeeds : UiEvent()
    }
}

package net.primal.android.bookmarks.list

import net.primal.android.feeds.domain.FeedSpecKind

interface BookmarksContract {
    data class UiState(
        val feedSpec: String,
        val feedSpecKind: FeedSpecKind,
    )

    sealed class UiEvent {
        data class ChangeFeedSpecKind(val feedSpecKind: FeedSpecKind) : UiEvent()
    }
}

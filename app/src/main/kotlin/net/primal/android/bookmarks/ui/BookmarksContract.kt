package net.primal.android.bookmarks.ui

import net.primal.android.feeds.domain.FeedSpecKind

interface BookmarksContract {
    data class UiState(
        val feedSpec: String? = null,
        val feedSpecKind: FeedSpecKind = FeedSpecKind.Notes,
    )

    sealed class UiEvent {
        data class ChangeFeedSpecKind(val feedSpecKind: FeedSpecKind) : UiEvent()
    }
}

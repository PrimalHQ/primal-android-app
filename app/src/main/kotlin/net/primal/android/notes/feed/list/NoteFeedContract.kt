package net.primal.android.notes.feed.list

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.FeedPostsSyncStats

interface NoteFeedContract {

    data class UiState(
        val mutedProfileIds: List<String> = emptyList(),
        val notes: Flow<PagingData<FeedPostUi>>,
        val paywall: Boolean = false,
        val feedPostsCount: Int = 0,
        val feedAutoRefresh: Boolean = false,
        val syncStats: FeedPostsSyncStats = FeedPostsSyncStats(),
        val shouldAnimateScrollToTop: Boolean? = null,
    )

    sealed class UiEvent {
        data object FeedScrolledToTop : UiEvent()
        data object StartPolling : UiEvent()
        data object StopPolling : UiEvent()
        data object ShowLatestNotes : UiEvent()
        data class UpdateCurrentTopVisibleNote(val noteId: String, val repostId: String? = null) : UiEvent()
    }
}

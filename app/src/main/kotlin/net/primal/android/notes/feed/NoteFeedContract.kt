package net.primal.android.notes.feed

import androidx.paging.PagingData
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.FeedPostsSyncStats

interface NoteFeedContract {

    data class UiState(
        val notes: Flow<PagingData<FeedPostUi>>,
        val feedPostsCount: Int = 0,
        val feedAutoRefresh: Boolean = false,
        val syncStats: FeedPostsSyncStats = FeedPostsSyncStats(),
        val topVisibleNote: Pair<String, String?>? = null,
        val latestNotesUpdatedTimestamp: Instant? = null,
    )

    sealed class UiEvent {
        data object FeedScrolledToTop : UiEvent()
        data object StartPolling : UiEvent()
        data object StopPolling : UiEvent()
        data object ShowLatestNotes : UiEvent()
        data class UpdateCurrentTopVisibleNote(val noteId: String, val repostId: String? = null) : UiEvent()
    }
}

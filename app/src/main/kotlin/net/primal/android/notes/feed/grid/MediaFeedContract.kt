package net.primal.android.notes.feed.grid

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.notes.feed.model.FeedPostUi

interface MediaFeedContract {

    data class UiState(
        val notes: Flow<PagingData<FeedPostUi>>,
    )
}

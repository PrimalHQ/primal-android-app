package net.primal.android.notes.feed

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.core.compose.feed.model.FeedPostUi

interface NoteFeedContract {

    data class UiState(
        val notes: Flow<PagingData<FeedPostUi>>,
    )

    sealed class UiEvent
}

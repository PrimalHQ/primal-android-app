package net.primal.android.events.reactions

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.events.ui.EventZapUiModel

interface ReactionsContract {
    data class UiState(
        val zaps: Flow<PagingData<EventZapUiModel>>,
        val loading: Boolean = true,
        val likes: List<EventActionUi> = emptyList(),
        val reposts: List<EventActionUi> = emptyList(),
    )
}

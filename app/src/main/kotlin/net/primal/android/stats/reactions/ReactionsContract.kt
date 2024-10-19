package net.primal.android.stats.reactions

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.stats.ui.EventZapUiModel

interface ReactionsContract {
    data class UiState(
        val zaps: Flow<PagingData<EventZapUiModel>>,
    )
}

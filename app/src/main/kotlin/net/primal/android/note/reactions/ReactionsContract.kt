package net.primal.android.note.reactions

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.note.ui.NoteZapUiModel

interface ReactionsContract {
    data class UiState(
        val zaps: Flow<PagingData<NoteZapUiModel>>,
    )
}

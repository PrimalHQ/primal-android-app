package net.primal.android.main.explore.zaps

import net.primal.android.main.explore.zaps.ui.ExploreZapNoteUi

interface ExploreZapsContract {
    data class UiState(
        val loading: Boolean = true,
        val zaps: List<ExploreZapNoteUi> = emptyList(),
    )

    sealed class UiEvent {
        data object RefreshZaps : UiEvent()
    }
}

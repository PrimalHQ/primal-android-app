package net.primal.android.explore.home.zaps

import net.primal.android.explore.home.zaps.ui.ExploreZapNoteUi

interface ExploreZapsContract {
    data class UiState(
        val loading: Boolean = true,
        val zaps: List<ExploreZapNoteUi> = emptyList(),
    )

    sealed class UiEvent {
        data object RefreshZaps : UiEvent()
    }
}

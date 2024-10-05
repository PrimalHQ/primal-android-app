package net.primal.android.explore.home.zaps

import net.primal.android.explore.api.model.ExploreZapData

interface ExploreZapsContract {
    data class UiState(
        val loading: Boolean = true,
        val zaps: List<ExploreZapData> = emptyList(),
    )

    sealed class UiEvent {
        data object RefreshZaps : UiEvent()
    }
}

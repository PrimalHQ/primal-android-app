package net.primal.android.premium.leaderboard.legend

import net.primal.android.premium.api.model.LegendLeaderboardOrderBy
import net.primal.android.premium.leaderboard.legend.ui.model.LeaderboardUiLegend

interface LegendLeaderboardContract {
    data class UiState(
        val leaderboardEntries: Map<LegendLeaderboardOrderBy, List<LeaderboardUiLegend>> = emptyMap(),
        val loading: Boolean = true,
        val error: Throwable? = null,
        val isActiveAccountLegend: Boolean = false,
    )

    sealed class UiEvent {
        data class FetchLeaderboardByOrder(val orderBy: LegendLeaderboardOrderBy) : UiEvent()
        data class RetryFetch(val orderBy: LegendLeaderboardOrderBy) : UiEvent()
    }
}

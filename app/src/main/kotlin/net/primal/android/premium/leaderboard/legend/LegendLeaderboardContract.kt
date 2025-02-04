package net.primal.android.premium.leaderboard.legend

import net.primal.android.premium.api.model.LeaderboardOrderBy
import net.primal.android.premium.leaderboard.domain.LeaderboardLegendEntry

interface LegendLeaderboardContract {
    data class UiState(
        val leaderboardEntries: Map<LeaderboardOrderBy, List<LeaderboardLegendEntry>> = emptyMap(),
        val loading: Boolean = true,
        val error: Throwable? = null,
        val isActiveAccountLegend: Boolean = false,
    )

    sealed class UiEvent {
        data class FetchLeaderboardByOrder(val orderBy: LeaderboardOrderBy) : UiEvent()
        data class RetryFetch(val orderBy: LeaderboardOrderBy) : UiEvent()
    }
}

package net.primal.android.premium.legend.leaderboard

import net.primal.android.premium.api.model.LeaderboardOrderBy
import net.primal.android.premium.legend.model.LegendLeaderboardEntry

interface LegendLeaderboardContract {
    data class UiState(
        val leaderboardEntries: Map<LeaderboardOrderBy, List<LegendLeaderboardEntry>> = emptyMap(),
        val loading: Boolean = true,
    )

    sealed class UiEvent {
        data class FetchLeaderboardByOrder(val orderBy: LeaderboardOrderBy) : UiEvent()
    }
}

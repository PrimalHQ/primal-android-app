package net.primal.android.premium.leaderboard.ogs

import net.primal.android.premium.leaderboard.domain.OGLeaderboardEntry

interface OGLeaderboardContract {
    data class UiState(
        val leaderboardEntries: List<OGLeaderboardEntry> = emptyList(),
        val loading: Boolean = true,
        val error: Throwable? = null,
        val isActiveAccountPremium: Boolean = false,
    )

    sealed class UiEvent {
        data object RetryFetch : UiEvent()
    }
}

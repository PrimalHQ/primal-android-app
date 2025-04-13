package net.primal.android.premium.leaderboard.ogs

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.domain.premium.OGLeaderboardEntry

interface OGLeaderboardContract {
    data class UiState(
        val leaderboardEntries: Flow<PagingData<OGLeaderboardEntry>>,
        val loading: Boolean = true,
        val error: Throwable? = null,
        val isActiveAccountPremium: Boolean = false,
    )
}

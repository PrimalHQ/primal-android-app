package net.primal.android.premium.legend.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.premium.api.model.LeaderboardOrderBy
import net.primal.android.premium.legend.leaderboard.LegendLeaderboardContract.UiState
import net.primal.android.premium.repository.PremiumRepository
import timber.log.Timber

@HiltViewModel
class LegendLeaderboardViewModel @Inject constructor(
    private val premiumRepository: PremiumRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    init {
        fetchLeaderboardByOrder(orderBy = LeaderboardOrderBy.LastDonation)
        fetchLeaderboardByOrder(orderBy = LeaderboardOrderBy.DonatedBtc)
    }

    private fun fetchLeaderboardByOrder(orderBy: LeaderboardOrderBy) =
        viewModelScope.launch {
            setState { copy(loading = true) }
            try {
                val entries = premiumRepository.fetchLegendLeaderboard(orderBy = orderBy)
                setState { copy(leaderboardEntries = leaderboardEntries + (orderBy to entries)) }
            } catch (error: WssException) {
                Timber.w(error)
            } finally {
                setState { copy(loading = false) }
            }
        }
}

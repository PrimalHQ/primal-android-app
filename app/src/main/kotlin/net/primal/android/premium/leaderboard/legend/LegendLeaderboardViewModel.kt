package net.primal.android.premium.leaderboard.legend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.premium.api.model.LegendLeaderboardOrderBy
import net.primal.android.premium.leaderboard.legend.LegendLeaderboardContract.UiEvent
import net.primal.android.premium.leaderboard.legend.LegendLeaderboardContract.UiState
import net.primal.android.premium.leaderboard.legend.ui.model.toUiModel
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.premium.utils.isPrimalLegendTier
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.domain.common.exception.NetworkException
import timber.log.Timber

@HiltViewModel
class LegendLeaderboardViewModel @Inject constructor(
    private val premiumRepository: PremiumRepository,
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observeActiveAccount()
    }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        isActiveAccountLegend = it.premiumMembership?.isPrimalLegendTier() == true,
                    )
                }
            }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.FetchLeaderboardByOrder -> {
                        if (state.value.leaderboardEntries[it.orderBy].isNullOrEmpty()) {
                            fetchLeaderboardByOrder(it.orderBy)
                        }
                    }

                    is UiEvent.RetryFetch -> fetchLeaderboardByOrder(it.orderBy)
                }
            }
        }

    private fun fetchLeaderboardByOrder(orderBy: LegendLeaderboardOrderBy) =
        viewModelScope.launch {
            setState { copy(loading = true, error = null) }
            try {
                val entries = premiumRepository
                    .fetchLegendLeaderboard(orderBy = orderBy, limit = 300)
                    .map { it.toUiModel() }
                setState { copy(leaderboardEntries = leaderboardEntries + (orderBy to entries)) }
            } catch (error: NetworkException) {
                Timber.w(error)
                setState { copy(error = error) }
            } finally {
                setState { copy(loading = false) }
            }
        }
}

package net.primal.android.premium.leaderboard.ogs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.premium.leaderboard.ogs.OGLeaderboardContract.UiEvent
import net.primal.android.premium.leaderboard.ogs.OGLeaderboardContract.UiState
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.premium.utils.isPremiumTier
import net.primal.android.premium.utils.isPrimalLegendTier
import net.primal.android.user.accounts.active.ActiveAccountStore
import timber.log.Timber

@HiltViewModel
class OGLeaderboardViewModel @Inject constructor(
    private val premiumRepository: PremiumRepository,
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        fetchLeaderboardByOrder()
        observeEvents()
        observeActiveAccount()
    }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        isActiveAccountPremium = it.premiumMembership?.isPremiumTier() == true ||
                            it.premiumMembership?.isPrimalLegendTier() == true,
                    )
                }
            }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.RetryFetch -> fetchLeaderboardByOrder()
                }
            }
        }

    private fun fetchLeaderboardByOrder() =
        viewModelScope.launch {
            setState { copy(loading = true, error = null) }
            try {
                val entries = premiumRepository.fetchPremiumLeaderboard()
                setState { copy(leaderboardEntries = entries) }
            } catch (error: WssException) {
                Timber.w(error)
                setState { copy(error = error) }
            } finally {
                setState { copy(loading = false) }
            }
        }
}

package net.primal.android.premium.leaderboard.ogs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.premium.api.paging.PremiumLeaderboardPagingSource
import net.primal.android.premium.leaderboard.domain.OGLeaderboardEntry
import net.primal.android.premium.leaderboard.ogs.OGLeaderboardContract.UiState
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.premium.utils.isPremiumTier
import net.primal.android.premium.utils.isPrimalLegendTier
import net.primal.android.user.accounts.active.ActiveAccountStore

@HiltViewModel
class OGLeaderboardViewModel @Inject constructor(
    private val premiumRepository: PremiumRepository,
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    companion object {
        private const val PAGE_SIZE = 100
    }

    private val leaderboardEntries: Flow<PagingData<OGLeaderboardEntry>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            enablePlaceholders = false,
            prefetchDistance = PAGE_SIZE / 2,
            initialLoadSize = PAGE_SIZE * 2,
        ),
        pagingSourceFactory = {
            PremiumLeaderboardPagingSource(
                premiumRepository = premiumRepository,
                pageSize = PAGE_SIZE,
            )
        },
    ).flow
        .cachedIn(viewModelScope)

    private val _state = MutableStateFlow(UiState(leaderboardEntries = leaderboardEntries))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    init {
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
}

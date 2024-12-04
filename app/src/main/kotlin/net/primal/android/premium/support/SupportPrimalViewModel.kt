package net.primal.android.premium.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.premium.support.SupportPrimalContract.UiState
import net.primal.android.premium.utils.isPremiumFreeTier
import net.primal.android.premium.utils.isPrimalLegendTier
import net.primal.android.user.accounts.active.ActiveAccountStore

@HiltViewModel
class SupportPrimalViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    init {
        observeActiveAccount()
    }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        primalName = it.premiumMembership?.premiumName,
                        isPrimalLegend = it.premiumMembership.isPrimalLegendTier(),
                        hasMembership = it.premiumMembership != null && !it.premiumMembership.isPremiumFreeTier(),
                    )
                }
            }
        }
}

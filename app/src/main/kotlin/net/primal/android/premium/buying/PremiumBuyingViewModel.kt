package net.primal.android.premium.buying

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
import net.primal.android.premium.buying.PremiumBuyingContract.UiEvent
import net.primal.android.premium.buying.PremiumBuyingContract.UiState
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.store.PrimalBillingClient
import timber.log.Timber

@HiltViewModel
class PremiumBuyingViewModel @Inject constructor(
    private val primalBillingClient: PrimalBillingClient,
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
        initBillingClient()
        subscribeToPurchases()
    }

    private fun initBillingClient() {
        viewModelScope.launch {
            primalBillingClient.refreshSubscriptionProducts()
            setState {
                copy(
                    subscriptions = primalBillingClient.subscriptionProducts,
                )
            }
        }
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.MoveToPremiumStage -> setState { copy(stage = it.stage) }
                    is UiEvent.SetPrimalName -> setState { copy(primalName = it.primalName) }
                }
            }
        }

    private fun subscribeToPurchases() =
        viewModelScope.launch {
            primalBillingClient.subscriptionPurchases.collect { purchase ->
                _state.value.primalName?.let { primalName ->
                    try {
                        premiumRepository.purchaseMembership(
                            userId = activeAccountStore.activeUserId(),
                            primalName = primalName,
                            purchase = purchase,
                        )
                        setState { copy(stage = PremiumBuyingContract.PremiumStage.Success) }
                    } catch (error: WssException) {
                        Timber.e(error)
                    }
                }
            }
        }
}

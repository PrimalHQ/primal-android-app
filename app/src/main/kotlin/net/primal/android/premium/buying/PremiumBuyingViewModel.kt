package net.primal.android.premium.buying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.core.compose.profile.model.asProfileDetailsUi
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.premium.buying.PremiumBuyingContract.UiEvent
import net.primal.android.premium.buying.PremiumBuyingContract.UiState
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.store.PrimalBillingClient
import net.primal.android.wallet.store.domain.InAppPurchaseException
import timber.log.Timber

@HiltViewModel
class PremiumBuyingViewModel @Inject constructor(
    private val primalBillingClient: PrimalBillingClient,
    private val premiumRepository: PremiumRepository,
    private val profileRepository: ProfileRepository,
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        initBillingClient()
        observeEvents()
        observePurchases()
        observeActiveProfile()
    }

    private fun initBillingClient() {
        viewModelScope.launch {
            primalBillingClient.fetchBillingProducts()
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
                    is UiEvent.ApplyPromoCode -> tryApplyPromoCode(it.promoCode)
                    UiEvent.ClearPromoCodeValidity -> setState { copy(promoCodeValidity = null) }
                    is UiEvent.RequestPurchase -> launchBillingFlow(it)
                }
            }
        }

    private fun observeActiveProfile() =
        viewModelScope.launch {
            profileRepository.observeProfile(profileId = activeAccountStore.activeUserId()).collect {
                setState {
                    copy(
                        profile = it.metadata?.asProfileDetailsUi(),
                        primalName = this.primalName ?: it.metadata?.handle,
                    )
                }
            }
        }

    private fun observePurchases() =
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

    private fun tryApplyPromoCode(promoCode: String) =
        viewModelScope.launch {
            setState { copy(isCheckingPromoCodeValidity = true) }
            delay(1.seconds)
            setState { copy(promoCodeValidity = true, isCheckingPromoCodeValidity = false) }
        }

    private fun launchBillingFlow(event: UiEvent.RequestPurchase) =
        viewModelScope.launch {
            try {
                primalBillingClient.launchSubscriptionBillingFlow(
                    subscriptionProduct = event.subscriptionProduct,
                    activity = event.activity,
                )
            } catch (error: InAppPurchaseException) {
                Timber.w(error)
                // TODO Handle error?
            }
        }
}

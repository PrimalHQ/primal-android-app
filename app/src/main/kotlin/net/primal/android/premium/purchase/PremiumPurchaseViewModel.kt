package net.primal.android.premium.purchase

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
import net.primal.android.premium.purchase.PremiumPurchaseContract.UiEvent
import net.primal.android.premium.purchase.PremiumPurchaseContract.UiState
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.store.PrimalBillingClient
import net.primal.android.wallet.store.domain.InAppPurchaseException
import timber.log.Timber

@HiltViewModel
class PremiumPurchaseViewModel @Inject constructor(
    activeAccountStore: ActiveAccountStore,
    private val profileRepository: ProfileRepository,
    private val primalBillingClient: PrimalBillingClient,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observeActiveProfile(profileId = activeAccountStore.activeUserId())
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.ApplyPromoCode -> tryApplyPromoCode(it.promoCode)
                    UiEvent.ClearPromoCodeValidity -> setState { copy(promoCodeValidity = null) }
                    is UiEvent.RequestPurchase -> launchBillingFlow(it)
                }
            }
        }

    private fun observeActiveProfile(profileId: String) =
        viewModelScope.launch {
            profileRepository.observeProfile(profileId = profileId).collect {
                setState { copy(profile = it.metadata?.asProfileDetailsUi()) }
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

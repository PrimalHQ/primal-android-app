package net.primal.android.premium.manage.order

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
import net.primal.android.nostr.notary.NostrReadOnlyMode
import net.primal.android.premium.domain.MembershipError
import net.primal.android.premium.manage.order.PremiumOrderHistoryContract.UiEvent
import net.primal.android.premium.manage.order.PremiumOrderHistoryContract.UiState
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.premium.utils.isPrimalLegendTier
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.store.PrimalBillingClient
import net.primal.android.wallet.store.domain.SubscriptionPurchase
import timber.log.Timber

@HiltViewModel
class PremiumOrderHistoryViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val billingClient: PrimalBillingClient,
    private val premiumRepository: PremiumRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private var purchase: SubscriptionPurchase? = null

    init {
        observeEvents()
        observeActiveAccount()
        fetchActiveSubscription()
        fetchOrderHistory()
    }

    private fun fetchOrderHistory() {
        viewModelScope.launch {
            setState { copy(fetchingHistory = true) }
            try {
                val orders = premiumRepository.fetchOrderHistory(userId = activeAccountStore.activeUserId())
                setState { copy(orders = orders) }
            } catch (error: NostrReadOnlyMode) {
                Timber.e(error)
            } catch (error: WssException) {
                Timber.e(error)
            } finally {
                setState { copy(fetchingHistory = false) }
            }
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.CancelSubscription -> cancelSubscription()
                }
            }
        }
    }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        primalName = it.premiumMembership?.premiumName ?: "",
                        isRecurringSubscription = it.premiumMembership?.recurring ?: false,
                        isLegend = it.premiumMembership.isPrimalLegendTier(),
                        subscriptionOrigin = it.premiumMembership?.origin,
                        expiresAt = it.premiumMembership?.expiresOn,
                    )
                }
            }
        }

    private fun fetchActiveSubscription() = viewModelScope.launch { fetchActivePurchase() }

    private suspend fun fetchActivePurchase() {
        val purchases = billingClient.queryActiveSubscriptions()
        purchase = purchases.firstOrNull()
    }

    private fun cancelSubscription() =
        viewModelScope.launch {
            setState { copy(cancellingSubscription = true) }
            val userId = activeAccountStore.activeUserId()
            fetchActivePurchase()
            val purchase = this@PremiumOrderHistoryViewModel.purchase
            if (purchase != null) {
                try {
                    premiumRepository.cancelSubscription(userId = userId, purchaseJson = purchase.playSubscriptionJson)
                    premiumRepository.fetchMembershipStatus(activeAccountStore.activeUserId())
                } catch (error: NostrReadOnlyMode) {
                    /* TODO(marko): not sure if we should do something here, I don't think the user can get here, silent fail might be good enough */
                    Timber.w(error)
                } catch (error: WssException) {
                    setState { copy(error = MembershipError.FailedToCancelSubscription(cause = error)) }
                } finally {
                    setState { copy(cancellingSubscription = false) }
                }
            } else {
                setState {
                    copy(
                        error = MembershipError.PlaySubscriptionPurchaseNotFound,
                        cancellingSubscription = false,
                    )
                }
            }
        }
}

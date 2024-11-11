package net.primal.android.premium.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.premium.domain.MembershipError
import net.primal.android.premium.home.PremiumHomeContract.UiEvent
import net.primal.android.premium.home.PremiumHomeContract.UiState
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.UserRepository
import net.primal.android.wallet.store.PrimalBillingClient
import net.primal.android.wallet.store.domain.SubscriptionPurchase
import timber.log.Timber

@HiltViewModel
class PremiumHomeViewModel @Inject constructor(
    private val billingClient: PrimalBillingClient,
    private val premiumRepository: PremiumRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val userRepository: UserRepository,
) : ViewModel() {

    private var purchase: SubscriptionPurchase? = null

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        fetchMembershipStatus()
        fetchActiveSubscription()
        observeActiveAccount()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.CancelSubscription -> cancelSubscription()
                    UiEvent.DismissError -> setState { copy(error = null) }
                    UiEvent.ApplyPrimalLightningAddress -> applyPrimalLightningAddress()
                    UiEvent.ApplyPrimalNostrAddress -> applyPrimalNostrAddress()
                }
            }
        }
    }

    private fun applyPrimalNostrAddress() =
        viewModelScope.launch {
            val premiumMembership = state.value.membership ?: return@launch
            val nip05 = premiumMembership.nostrAddress

            try {
                userRepository.setNostrAddress(userId = activeAccountStore.activeUserId(), nostrAddress = nip05)
            } catch (error: WssException) {
                Timber.w(error)
                setState { copy(error = MembershipError.ProfileMetadataNotFound) }
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setState { copy(error = MembershipError.FailedToApplyNostrAddress) }
            }
        }

    private fun applyPrimalLightningAddress() =
        viewModelScope.launch {
            val premiumMembership = state.value.membership ?: return@launch
            val lud16 = premiumMembership.lightningAddress

            try {
                userRepository.setLightningAddress(userId = activeAccountStore.activeUserId(), lightningAddress = lud16)
            } catch (error: WssException) {
                Timber.w(error)
                setState { copy(error = MembershipError.ProfileMetadataNotFound) }
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setState { copy(error = MembershipError.FailedToApplyLightningAddress) }
            }
        }

    private fun fetchActiveSubscription() = viewModelScope.launch { fetchActivePurchase() }

    private suspend fun fetchActivePurchase() {
        val purchases = billingClient.queryActiveSubscriptions()
        purchase = purchases.firstOrNull()
    }

    private fun fetchMembershipStatus() =
        viewModelScope.launch {
            try {
                premiumRepository.fetchMembershipStatus(activeAccountStore.activeUserId())
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        displayName = it.authorDisplayName,
                        avatarCdnImage = it.avatarCdnImage,
                        profileNostrAddress = it.internetIdentifier,
                        profileLightningAddress = it.lightningAddress,
                        membership = it.premiumMembership,
                    )
                }
            }
        }

    private fun cancelSubscription() =
        viewModelScope.launch {
            val userId = activeAccountStore.activeUserId()
            fetchActivePurchase()
            val purchase = this@PremiumHomeViewModel.purchase
            if (purchase != null) {
                try {
                    premiumRepository.cancelSubscription(userId = userId, purchaseJson = purchase.playSubscriptionJson)
                } catch (error: WssException) {
                    setState { copy(error = MembershipError.FailedToCancelSubscription(cause = error)) }
                }
                fetchMembershipStatus()
            } else {
                setState { copy(error = MembershipError.PlaySubscriptionPurchaseNotFound) }
            }
        }
}

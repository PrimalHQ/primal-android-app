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
                    UiEvent.ApplyPrimalLightningAddress -> applyPrimalLightningAddress()
                    UiEvent.ApplyPrimalNostrAddress -> applyPrimalNostrAddress()
                    UiEvent.DismissError -> setState { copy(error = null) }
                }
            }
        }
    }

    private fun applyPrimalNostrAddress() =
        viewModelScope.launch {
            val premiumMembership = state.value.membership ?: return@launch
            val nip05 = premiumMembership.premiumName.constructPrimalNostrAddress()

            try {
                userRepository.setNostrAddress(userId = activeAccountStore.activeUserId(), nostrAddress = nip05)
            } catch (error: WssException) {
                Timber.w(error)
                setState { copy(error = PremiumHomeContract.ApplyError.ProfileMetadataNotFound) }
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setState { copy(error = PremiumHomeContract.ApplyError.FailedToApplyNostrAddress) }
            }
        }

    private fun applyPrimalLightningAddress() =
        viewModelScope.launch {
            val premiumMembership = state.value.membership ?: return@launch
            val lud16 = premiumMembership.premiumName.constructPrimalLightningAddress()

            try {
                userRepository.setLightningAddress(userId = activeAccountStore.activeUserId(), lightningAddress = lud16)
            } catch (error: WssException) {
                Timber.w(error)
                setState { copy(error = PremiumHomeContract.ApplyError.ProfileMetadataNotFound) }
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setState { copy(error = PremiumHomeContract.ApplyError.FailedToApplyLightningAddress) }
            }
        }

    private fun fetchActiveSubscription() {
        viewModelScope.launch {
            val purchases = billingClient.queryActiveSubscriptions()
            purchase = purchases.firstOrNull()
        }
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
            purchase?.let {
                try {
                    premiumRepository.cancelSubscription(
                        userId = activeAccountStore.activeUserId(),
                        purchaseJson = it.playSubscriptionJson,
                    )
                } catch (error: WssException) {
                    Timber.e(error)
                }
            }
        }

    private fun String.constructPrimalNostrAddress() = "$this@primal.net"

    private fun String.constructPrimalLightningAddress() = "$this.@primal.net"
}

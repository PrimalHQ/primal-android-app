package net.primal.android.premium.buying

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.core.compose.profile.model.asProfileDetailsUi
import net.primal.android.core.utils.isGoogleBuild
import net.primal.android.navigation.FROM_ORIGIN_PREMIUM_BADGE
import net.primal.android.navigation.buyingPremiumFromOrigin
import net.primal.android.navigation.extendExistingPremiumName
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.notary.MissingPrivateKeyException
import net.primal.android.premium.buying.PremiumBuyingContract.PremiumStage
import net.primal.android.premium.buying.PremiumBuyingContract.SideEffect
import net.primal.android.premium.buying.PremiumBuyingContract.UiEvent
import net.primal.android.premium.buying.PremiumBuyingContract.UiState
import net.primal.android.premium.domain.MembershipError
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.premium.utils.hasPremiumMembership
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.UserRepository
import net.primal.android.wallet.store.PrimalBillingClient
import net.primal.android.wallet.store.domain.InAppPurchaseException
import net.primal.android.wallet.store.domain.SubscriptionPurchase
import timber.log.Timber

@HiltViewModel
class PremiumBuyingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val primalBillingClient: PrimalBillingClient,
    private val premiumRepository: PremiumRepository,
    private val profileRepository: ProfileRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val userRepository: UserRepository,
) : ViewModel() {

    private var purchase: SubscriptionPurchase? = null

    private val _state = MutableStateFlow(
        UiState(
            isExtendingPremium = savedStateHandle.extendExistingPremiumName != null,
            isPremiumBadgeOrigin = savedStateHandle.buyingPremiumFromOrigin == FROM_ORIGIN_PREMIUM_BADGE,
            primalName = savedStateHandle.extendExistingPremiumName,
            stage = if (savedStateHandle.extendExistingPremiumName != null) {
                PremiumStage.Purchase
            } else {
                PremiumStage.Home
            },
        ),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effects = Channel<SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effects.send(effect) }

    init {
        checkMembershipStatus()
        observeEvents()
        observePurchases()
        observeActiveProfile()
        initBillingClient()
        markPremiumBuyingOpened()
    }

    private fun markPremiumBuyingOpened() {
        viewModelScope.launch {
            userRepository.updateBuyPremiumTimestamp(userId = activeAccountStore.activeUserId())
        }
    }

    private fun checkMembershipStatus() =
        viewModelScope.launch {
            if (activeAccountStore.activeUserAccount().hasPremiumMembership()) {
                setEffect(SideEffect.NavigateToPremiumHome)
            } else {
                try {
                    premiumRepository.fetchMembershipStatus(userId = activeAccountStore.activeUserId())?.let {
                        if (it.hasPremiumMembership()) {
                            setEffect(SideEffect.NavigateToPremiumHome)
                        }
                    }
                } catch (error: WssException) {
                    Timber.w(error)
                }
            }
        }

    private fun initBillingClient() {
        viewModelScope.launch {
            try {
                if (isGoogleBuild()) {
                    val subscriptionProducts = primalBillingClient.querySubscriptionProducts()
                    setState { copy(loading = false, subscriptions = subscriptionProducts) }
                } else {
                    premiumRepository.fetchMembershipProducts()
                    setState { copy(loading = false) }
                }

                fetchActiveSubscription()
            } catch (error: WssException) {
                Timber.w(error)
            }
        }
    }

    private fun fetchActiveSubscription() {
        viewModelScope.launch {
            val purchases = primalBillingClient.queryActiveSubscriptions()
            purchase = purchases.firstOrNull()
            setState { copy(hasActiveSubscription = purchase != null) }
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
                    UiEvent.RestoreSubscription -> restorePurchase()
                    UiEvent.DismissError -> setState { copy(error = null) }
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
            val userId = activeAccountStore.activeUserId()
            primalBillingClient.subscriptionPurchases.collect { purchase ->
                _state.value.primalName?.let { primalName ->
                    try {
                        premiumRepository.purchaseMembership(
                            userId = userId,
                            primalName = primalName,
                            purchase = purchase,
                        )
                        setState { copy(stage = PremiumStage.Success) }
                    } catch (error: MissingPrivateKeyException) {
                        Timber.w(error)
                    } catch (error: WssException) {
                        Timber.e(error)
                        this@PremiumBuyingViewModel.purchase = purchase
                        setState {
                            copy(
                                hasActiveSubscription = true,
                                error = MembershipError.FailedToProcessSubscriptionPurchase(cause = error),
                            )
                        }
                    }

                    runCatching { premiumRepository.fetchMembershipStatus(userId = userId) }
                }
            }
        }

    @Suppress("unused")
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
            }
        }

    private fun restorePurchase() =
        viewModelScope.launch {
            val userId = activeAccountStore.activeUserId()
            val primalName = _state.value.primalName
            val existingPurchase = purchase
            if (primalName != null && existingPurchase != null) {
                try {
                    premiumRepository.purchaseMembership(
                        userId = userId,
                        primalName = primalName,
                        purchase = existingPurchase,
                    )
                    setState { copy(stage = PremiumStage.Success) }
                    premiumRepository.fetchMembershipStatus(userId = userId)
                } catch (error: MissingPrivateKeyException) {
                    Timber.e(error)
                } catch (error: WssException) {
                    Timber.e(error)
                }
            }
        }
}

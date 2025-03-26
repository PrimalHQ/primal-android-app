package net.primal.android.premium.legend.become

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.navigation.FROM_ORIGIN_PREMIUM_BADGE
import net.primal.android.navigation.buyingPremiumFromOrigin
import net.primal.android.nostr.notary.exceptions.MissingPrivateKey
import net.primal.android.nostr.notary.exceptions.SignException
import net.primal.android.premium.legend.become.PremiumBecomeLegendContract.Companion.LEGEND_THRESHOLD_IN_USD
import net.primal.android.premium.legend.become.PremiumBecomeLegendContract.UiEvent
import net.primal.android.premium.legend.become.PremiumBecomeLegendContract.UiState
import net.primal.android.premium.legend.subscription.PurchaseMonitor
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.repository.WalletRepository
import net.primal.android.wallet.utils.parseBitcoinPaymentInstructions
import net.primal.core.networking.sockets.errors.WssException
import timber.log.Timber

@HiltViewModel
class PremiumBecomeLegendViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activeAccountStore: ActiveAccountStore,
    private val walletRepository: WalletRepository,
    private val premiumRepository: PremiumRepository,
    private val purchaseMonitor: PurchaseMonitor,
) : ViewModel() {

    private companion object {
        private const val BTC_DECIMAL_PLACES = 8
    }

    private val _state = MutableStateFlow(
        UiState(
            isPremiumBadgeOrigin = savedStateHandle.buyingPremiumFromOrigin == FROM_ORIGIN_PREMIUM_BADGE,
        ),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observeActiveAccount()
        fetchExchangeRate()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.GoToFindPrimalNameStage -> setState {
                        copy(stage = PremiumBecomeLegendContract.BecomeLegendStage.PickPrimalName)
                    }

                    UiEvent.ShowAmountEditor -> {
                        val state = _state.value
                        if (!state.arePaymentInstructionsAvailable() && state.primalName != null) {
                            fetchLegendPaymentInstructions(primalName = state.primalName)
                        }
                        setState { copy(stage = PremiumBecomeLegendContract.BecomeLegendStage.PickAmount) }
                    }

                    UiEvent.GoBackToIntro -> setState {
                        copy(stage = PremiumBecomeLegendContract.BecomeLegendStage.Intro)
                    }

                    UiEvent.ShowPaymentInstructions -> setState {
                        copy(stage = PremiumBecomeLegendContract.BecomeLegendStage.Payment)
                    }

                    is UiEvent.UpdateSelectedAmount -> {
                        updatePaymentAmount(
                            amount = it.newAmount.toBigDecimal().setScale(BTC_DECIMAL_PLACES, RoundingMode.HALF_UP),
                        )
                    }

                    UiEvent.StartPurchaseMonitor -> startPurchaseMonitor()

                    UiEvent.StopPurchaseMonitor -> stopPurchaseMonitor()

                    is UiEvent.PrimalNamePicked -> setState { copy(primalName = it.primalName) }

                    UiEvent.FetchPaymentInstructions -> {
                        _state.value.primalName?.let { primalName ->
                            fetchLegendPaymentInstructions(primalName = primalName)
                        }
                    }
                }
            }
        }

    private fun updatePaymentAmount(amount: BigDecimal) {
        setState {
            copy(
                selectedAmountInBtc = amount,
                qrCodeValue = "bitcoin:${this.bitcoinAddress}?amount=$amount",
            )
        }
    }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        avatarCdnImage = it.avatarCdnImage,
                        userHandle = it.userDisplayName,
                        isPremiumUser = it.premiumMembership?.isExpired() == false,
                        primalName = it.premiumMembership?.premiumName,
                    )
                }
            }
        }

    private fun fetchExchangeRate() {
        viewModelScope.launch {
            try {
                val btcRate = walletRepository.getExchangeRate(
                    userId = activeAccountStore.activeUserId(),
                )
                setState {
                    copy(
                        minLegendThresholdInBtc = if (this.minLegendThresholdInBtc == BigDecimal.ZERO) {
                            (LEGEND_THRESHOLD_IN_USD / btcRate).toBigDecimal()
                        } else {
                            this.minLegendThresholdInBtc
                        },
                        selectedAmountInBtc = if (this.selectedAmountInBtc == BigDecimal.ZERO) {
                            minLegendThresholdInBtc
                        } else {
                            this.selectedAmountInBtc
                        },
                        exchangeBtcUsdRate = btcRate,
                    )
                }
            } catch (error: SignException) {
                Timber.e(error)
            } catch (error: WssException) {
                Timber.e(error)
            }
        }
    }

    private fun fetchLegendPaymentInstructions(primalName: String) =
        viewModelScope.launch {
            try {
                setState { copy(isFetchingPaymentInstructions = true) }
                val response = premiumRepository.fetchPrimalLegendPaymentInstructions(
                    userId = activeAccountStore.activeUserId(),
                    primalName = primalName,
                    amountUsd = null,
                )

                val minAmount = response.amountBtc.toBigDecimal().setScale(BTC_DECIMAL_PLACES, RoundingMode.HALF_UP)
                setState {
                    copy(
                        minLegendThresholdInBtc = minAmount,
                        selectedAmountInBtc = minAmount,
                        bitcoinAddress = response.qrCode.parseBitcoinPaymentInstructions()?.address,
                        membershipQuoteId = response.membershipQuoteId,
                    )
                }
                updatePaymentAmount(amount = minAmount)

                startPurchaseMonitor()
            } catch (error: SignException) {
                Timber.e(error)
            } catch (error: WssException) {
                Timber.e(error)
            } finally {
                setState { copy(isFetchingPaymentInstructions = false) }
            }
        }

    private fun startPurchaseMonitor() {
        _state.value.membershipQuoteId?.let {
            purchaseMonitor.startMonitor(
                scope = viewModelScope,
                quoteId = it,
            ) {
                fetchMembershipStatus()
                setState { copy(stage = PremiumBecomeLegendContract.BecomeLegendStage.Success) }
            }
        }
    }

    private fun stopPurchaseMonitor() {
        purchaseMonitor.stopMonitor(viewModelScope)
    }

    private fun fetchMembershipStatus() =
        viewModelScope.launch {
            try {
                premiumRepository.fetchMembershipStatus(activeAccountStore.activeUserId())
            } catch (error: WssException) {
                Timber.w(error)
            }
        }
}

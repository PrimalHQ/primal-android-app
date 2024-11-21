package net.primal.android.premium.legend.become

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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.networking.di.PrimalWalletApiClient
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalSocketSubscription
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.ext.takeContentOrNull
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.premium.api.model.MembershipPurchaseMonitorRequestBody
import net.primal.android.premium.api.model.MembershipPurchaseMonitorResponse
import net.primal.android.premium.legend.become.PremiumBecomeLegendContract.Companion.LEGEND_THRESHOLD_IN_USD
import net.primal.android.premium.legend.become.PremiumBecomeLegendContract.UiEvent
import net.primal.android.premium.legend.become.PremiumBecomeLegendContract.UiState
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.repository.WalletRepository
import net.primal.android.wallet.utils.parseBitcoinPaymentInstructions
import timber.log.Timber

@HiltViewModel
class PremiumBecomeLegendViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val walletRepository: WalletRepository,
    private val premiumRepository: PremiumRepository,
    @PrimalWalletApiClient private val walletApiClient: PrimalApiClient,
) : ViewModel() {

    private companion object {
        private const val BTC_DECIMAL_PLACES = 8
    }

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private var monitorSubscription: PrimalSocketSubscription<MembershipPurchaseMonitorResponse>? = null
    private var monitorMutex = Mutex()

    init {
        observeEvents()
        observeActiveAccount()
        fetchExchangeRate()
        fetchLegendPaymentInstructions()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.ShowAmountEditor -> {
                        if (_state.value.minLegendThresholdInBtc != BigDecimal.ONE) {
                            setState { copy(stage = PremiumBecomeLegendContract.BecomeLegendStage.PickAmount) }
                        }
                    }

                    UiEvent.GoBackToIntro -> setState {
                        copy(stage = PremiumBecomeLegendContract.BecomeLegendStage.Intro)
                    }

                    UiEvent.ShowPaymentInstructions -> setState {
                        copy(stage = PremiumBecomeLegendContract.BecomeLegendStage.Payment)
                    }

                    is UiEvent.UpdateSelectedAmount -> {
                        val newAmountInBtc = it.newAmount.toBigDecimal().setScale(
                            BTC_DECIMAL_PLACES,
                            RoundingMode.HALF_UP,
                        )
                        setState {
                            copy(
                                selectedAmountInBtc = newAmountInBtc,
                                qrCodeValue = "bitcoin:${this.bitcoinAddress}?amount=$newAmountInBtc",
                            )
                        }
                    }

                    UiEvent.StartPurchaseMonitor -> startPurchaseMonitorIfStopped()

                    UiEvent.StopPurchaseMonitor -> stopPurchaseMonitor()
                }
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
            } catch (error: WssException) {
                Timber.e(error)
            }
        }
    }

    private fun fetchLegendPaymentInstructions() {
        _state.value.membership?.premiumName?.let { primalName ->
            viewModelScope.launch {
                try {
                    val response = premiumRepository.fetchPrimalLegendPaymentInstructions(
                        userId = activeAccountStore.activeUserId(),
                        primalName = primalName,
                    )

                    setState {
                        copy(
                            minLegendThresholdInBtc = response.amountBtc.toBigDecimal(),
                            selectedAmountInBtc = response.amountBtc.toBigDecimal(),
                            bitcoinAddress = response.qrCode.parseBitcoinPaymentInstructions()?.address,
                            membershipQuoteId = response.membershipQuoteId,
                        )
                    }

                    startPurchaseMonitorIfStopped()
                } catch (error: WssException) {
                    Timber.e(error)
                }
            }
        }
    }

    private fun subscribeToPurchaseMonitor(quoteId: String) =
        PrimalSocketSubscription.launch(
            scope = viewModelScope,
            primalApiClient = walletApiClient,
            cacheFilter = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET_MEMBERSHIP_PURCHASE_MONITOR,
                optionsJson = NostrJson.encodeToString(
                    MembershipPurchaseMonitorRequestBody(membershipQuoteId = quoteId),
                ),
            ),
            transformer = {
                if (primalEvent?.kind == NostrEventKind.PrimalMembershipPurchaseMonitor.value) {
                    primalEvent.takeContentOrNull<MembershipPurchaseMonitorResponse>()
                } else {
                    null
                }
            },
        ) {
            if (it.completedAt != null) {
                fetchMembershipStatus()
                setState { copy(stage = PremiumBecomeLegendContract.BecomeLegendStage.Success) }
                stopPurchaseMonitor()
            }
        }

    private fun startPurchaseMonitorIfStopped() {
        viewModelScope.launch {
            monitorMutex.withLock {
                if (monitorSubscription == null) {
                    val quoteId = _state.value.membershipQuoteId
                    if (quoteId != null) {
                        monitorSubscription = subscribeToPurchaseMonitor(quoteId = quoteId)
                    }
                }
            }
        }
    }

    private fun stopPurchaseMonitor() {
        viewModelScope.launch {
            monitorMutex.withLock {
                monitorSubscription?.unsubscribe()
                monitorSubscription = null
            }
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
}

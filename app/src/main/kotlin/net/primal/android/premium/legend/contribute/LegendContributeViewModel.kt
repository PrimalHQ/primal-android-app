package net.primal.android.premium.legend.contribute

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
import net.primal.android.core.utils.getMaximumUsdAmount
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.premium.legend.contribute.LegendContributeContract.LegendContributeState
import net.primal.android.premium.legend.contribute.LegendContributeContract.UiEvent
import net.primal.android.premium.legend.contribute.LegendContributeContract.UiState
import net.primal.android.premium.legend.subscription.PurchaseMonitorManager
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.domain.CurrencyMode
import net.primal.android.wallet.domain.not
import net.primal.android.wallet.repository.ExchangeRateHandler
import net.primal.android.wallet.utils.CurrencyConversionUtils.fromSatsToUsd
import net.primal.android.wallet.utils.CurrencyConversionUtils.fromUsdToSats
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc
import net.primal.android.wallet.utils.formatUsdZeros
import net.primal.android.wallet.utils.parseBitcoinPaymentInstructions
import net.primal.android.wallet.utils.parseSatsToUsd
import net.primal.android.wallet.utils.parseUsdToSats
import timber.log.Timber

@HiltViewModel
class LegendContributeViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val exchangeRateHandler: ExchangeRateHandler,
    private val premiumRepository: PremiumRepository,
    private val purchaseMonitorManager: PurchaseMonitorManager,
) : ViewModel() {

    private companion object {
        private const val BTC_DECIMAL_PLACES = 8
    }

    private val _state = MutableStateFlow(UiState())

    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        fetchExchangeRate()
        observeUsdExchangeRate()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.GoBackToIntro -> setState {
                        copy(stage = LegendContributeState.Intro)
                    }

                    UiEvent.GoBackToPickAmount -> setState {
                        copy(stage = LegendContributeState.PickAmount)
                    }

                    UiEvent.GoBackToPaymentInstructions -> setState {
                        copy(stage = LegendContributeState.Payment)
                    }

                    UiEvent.ChangeCurrencyMode -> setState {
                        copy(currencyMode = !state.value.currencyMode)
                    }

                    UiEvent.ShowPaymentInstructions -> setState {
                        copy(stage = LegendContributeState.Payment)
                    }

                    UiEvent.ShowSuccess -> setState {
                        copy(stage = LegendContributeState.Success)
                    }

                    UiEvent.FetchPaymentInstructions -> {
                        fetchLegendPaymentInstructions()
                    }

                    UiEvent.StartPurchaseMonitor -> startPurchaseMonitor()

                    UiEvent.StopPurchaseMonitor -> stopPurchaseMonitor()

                    is UiEvent.ShowAmountEditor -> setState {
                        copy(
                            paymentMethod = it.paymentMethod,
                            stage = LegendContributeState.PickAmount,
                        )
                    }

                    is UiEvent.AmountChanged -> {
                        updateAmount(amount = it.amount)
                    }
                }
            }
        }

    private fun observeUsdExchangeRate() =
        viewModelScope.launch {
            exchangeRateHandler.usdExchangeRate.collect {
                setState {
                    copy(
                        currentExchangeRate = it,
                        maximumUsdAmount = getMaximumUsdAmount(it),
                        amountInUsd = BigDecimal(_state.value.amountInSats)
                            .fromSatsToUsd(it)
                            .formatUsdZeros(),
                    )
                }
            }
        }

    private fun fetchExchangeRate() =
        viewModelScope.launch {
            exchangeRateHandler.updateExchangeRate(
                userId = activeAccountStore.activeUserId(),
            )
        }

    private fun fetchLegendPaymentInstructions() =
        viewModelScope.launch {
            try {
                setState { copy(isFetchingPaymentInstructions = true) }

                val response = premiumRepository.fetchPrimalLegendPaymentInstructions(
                    userId = activeAccountStore.activeUserId(),
                    primalName = "",
                    onChain = state.value.paymentMethod == LegendContributeContract.PaymentMethod.OnChainBitcoin,
                )

                setState {
                    copy(
                        bitcoinAddress = response.qrCode.parseBitcoinPaymentInstructions()?.address,
                        membershipQuoteId = response.membershipQuoteId,
                    )
                }

                val currentAmount = if (state.value.currencyMode == CurrencyMode.SATS) {
                    state.value.amountInSats
                } else {
                    state.value.amountInUsd
                }

                updateAmount(currentAmount)

                startPurchaseMonitor()
            } catch (error: WssException) {
                Timber.e(error)
            } finally {
                setState { copy(isFetchingPaymentInstructions = false) }
            }
        }

    private fun updateAmount(amount: String = "0") =
        when (_state.value.currencyMode) {
            CurrencyMode.SATS -> {
                setState {
                    copy(
                        amountInSats = amount,
                        amountInUsd = amount.parseSatsToUsd(state.value.currentExchangeRate),
                        qrCodeValue = "bitcoin:${this.bitcoinAddress}?amount=${
                            amount.toULong().toBtc().toBigDecimal().formatToBtcString(BTC_DECIMAL_PLACES)
                        }",
                    )
                }
            }

            CurrencyMode.FIAT -> {
                setState {
                    copy(
                        amountInSats = amount.parseUsdToSats(state.value.currentExchangeRate),
                        amountInUsd = amount,
                        qrCodeValue = "bitcoin:${this.bitcoinAddress}?amount=${
                            BigDecimal(amount).fromUsdToSats(state.value.currentExchangeRate)
                                .toBtc().toBigDecimal().formatToBtcString(BTC_DECIMAL_PLACES)
                        }",
                    )
                }
            }
        }

    private fun startPurchaseMonitor() {
        purchaseMonitorManager.startMonitor(
            scope = viewModelScope,
            quoteId = _state.value.membershipQuoteId,
        ) {
            setState { copy(stage = LegendContributeState.Success) }
        }
    }

    private fun stopPurchaseMonitor() {
        purchaseMonitorManager.stopMonitor(viewModelScope)
    }

    private fun BigDecimal.formatToBtcString(scale: Int): String {
        return this.setScale(scale, RoundingMode.HALF_UP).toPlainString()
    }
}

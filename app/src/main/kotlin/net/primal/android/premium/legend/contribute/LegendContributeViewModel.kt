package net.primal.android.premium.legend.contribute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.core.utils.getMaximumUsdAmount
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.premium.legend.contribute.LegendContributeContract.LegendContributeState
import net.primal.android.premium.legend.contribute.LegendContributeContract.PaymentMethod
import net.primal.android.premium.legend.contribute.LegendContributeContract.UiEvent
import net.primal.android.premium.legend.contribute.LegendContributeContract.UiState
import net.primal.android.premium.legend.subscription.PurchaseMonitorManager
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.api.model.WithdrawRequestBody
import net.primal.android.wallet.domain.CurrencyMode
import net.primal.android.wallet.domain.SubWallet
import net.primal.android.wallet.domain.not
import net.primal.android.wallet.repository.ExchangeRateHandler
import net.primal.android.wallet.repository.WalletRepository
import net.primal.android.wallet.utils.CurrencyConversionUtils.fromSatsToUsd
import net.primal.android.wallet.utils.formatUsdZeros
import net.primal.android.wallet.utils.parseBitcoinPaymentInstructions
import net.primal.android.wallet.utils.parseLightningPaymentInstructions
import net.primal.android.wallet.utils.parseSatsToUsd
import net.primal.android.wallet.utils.parseUsdToSats
import timber.log.Timber

@HiltViewModel
class LegendContributeViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val exchangeRateHandler: ExchangeRateHandler,
    private val premiumRepository: PremiumRepository,
    private val walletRepository: WalletRepository,
    private val purchaseMonitorManager: PurchaseMonitorManager,
) : ViewModel() {

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
                        copy(
                            stage = LegendContributeState.PickAmount,
                            lightningAddress = null,
                            bitcoinAddress = null,
                            membershipQuoteId = null,
                        )
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

                    UiEvent.PrimalWalletPayment -> {
                        withdrawViaPrimalWallet()
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
                    primalName = "qa",
                    onChain = state.value.paymentMethod == PaymentMethod.OnChainBitcoin,
                    amountUsd = state.value.amountInUsd,
                )

                if (state.value.paymentMethod == PaymentMethod.OnChainBitcoin) {
                    setState {
                        copy(
                            bitcoinAddress = response.qrCode.parseBitcoinPaymentInstructions()?.address,
                            membershipQuoteId = response.membershipQuoteId,
                            qrCodeValue = response.qrCode,
                        )
                    }
                } else {
                    setState {
                        copy(
                            lightningAddress = response.qrCode.parseLightningPaymentInstructions(),
                            membershipQuoteId = response.membershipQuoteId,
                            qrCodeValue = response.qrCode,
                        )
                    }
                }

                startPurchaseMonitor()
            } catch (error: WssException) {
                Timber.e(error)
            } finally {
                setState { copy(isFetchingPaymentInstructions = false) }
            }
        }

    private fun withdrawViaPrimalWallet() =
        viewModelScope.launch {
            try {
                setState { copy(isFetchingWithdrawRequest = true) }

                if (state.value.paymentMethod == PaymentMethod.OnChainBitcoin) {
                    walletRepository.withdraw(
                        userId = activeAccountStore.activeUserId(),
                        body = WithdrawRequestBody(
                            subWallet = SubWallet.Open,
                            targetBtcAddress = state.value.bitcoinAddress
                        ),
                    )
                } else {
                    walletRepository.withdraw(
                        userId = activeAccountStore.activeUserId(),
                        body = WithdrawRequestBody(
                            subWallet = SubWallet.Open,
                            lnInvoice = state.value.lightningAddress
                        ),
                    )
                }

            } catch (error: WssException) {
                Timber.e(error)
            } finally {
                setState { copy(isFetchingWithdrawRequest = false) }
            }
        }

    private fun updateAmount(amount: String) =
        when (_state.value.currencyMode) {
            CurrencyMode.SATS -> {
                setState {
                    copy(
                        amountInSats = amount,
                        amountInUsd = amount.parseSatsToUsd(state.value.currentExchangeRate),
                    )
                }
            }

            CurrencyMode.FIAT -> {
                setState {
                    copy(
                        amountInSats = amount.parseUsdToSats(state.value.currentExchangeRate),
                        amountInUsd = amount,
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
}

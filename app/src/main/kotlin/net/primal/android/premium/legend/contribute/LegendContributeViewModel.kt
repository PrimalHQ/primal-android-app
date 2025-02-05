package net.primal.android.premium.legend.contribute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
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
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())

    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observeActiveAccount()
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
                        val state = _state.value

                        if (state.primalName != null) {
                            fetchLegendPaymentInstructions(primalName = state.primalName)
                        }
                    }

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

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        primalName = it.premiumMembership?.premiumName,
                    )
                }
            }
        }

    private fun fetchLegendPaymentInstructions(primalName: String) =
        viewModelScope.launch {
            try {
                setState { copy(isFetchingPaymentInstructions = true) }

                val response = premiumRepository.fetchPrimalLegendContributeInstructions(
                    onChain = state.value.paymentMethod == LegendContributeContract.PaymentMethod.OnChainBitcoin,
                )

                setState {
                    copy(
                        bitcoinAddress = response.qrCode.parseBitcoinPaymentInstructions()?.address,
                    )
                }

                updateAmount("0")
                Timber.i("Here is the response, with $primalName: $response")

//                startPurchaseMonitorIfStopped()
            } catch (error: WssException) {
                Timber.e("Here is the response, with $primalName: $error")
            } finally {
                setState { copy(isFetchingPaymentInstructions = false) }
            }
        }

    private fun updateAmount(amount: String) =
        when (_state.value.currencyMode) {
            CurrencyMode.SATS -> {
                setState {
                    copy(
                        amountInSats = amount,
                        amountInUsd = amount.parseSatsToUsd(state.value.currentExchangeRate),
                        qrCodeValue = "bitcoin:${this.bitcoinAddress}?amount=${amount.toULong().toBtc()}",
                    )
                }
            }

            CurrencyMode.FIAT -> {
                setState {
                    copy(
                        amountInSats = amount.parseUsdToSats(state.value.currentExchangeRate),
                        amountInUsd = amount,
                        qrCodeValue = "bitcoin:${this.bitcoinAddress}?amount=${
                            BigDecimal(amount).fromUsdToSats(state.value.currentExchangeRate).toBtc()
                        }",
                    )
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
}

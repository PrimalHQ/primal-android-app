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
import net.primal.android.premium.legend.contribute.LegendContributeContract.LegendContributeState
import net.primal.android.premium.legend.contribute.LegendContributeContract.UiEvent
import net.primal.android.premium.legend.contribute.LegendContributeContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.domain.CurrencyMode
import net.primal.android.wallet.domain.not
import net.primal.android.wallet.repository.ExchangeRateHandler
import net.primal.android.wallet.utils.CurrencyConversionUtils.fromSatsToUsd
import net.primal.android.wallet.utils.formatUsdZeros
import net.primal.android.wallet.utils.parseSatsToUsd
import net.primal.android.wallet.utils.parseUsdToSats

@HiltViewModel
class LegendContributeViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val exchangeRateHandler: ExchangeRateHandler,
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

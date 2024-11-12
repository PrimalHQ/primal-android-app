package net.primal.android.premium.legend

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
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.premium.legend.PremiumBecomeLegendContract.Companion.LEGEND_THRESHOLD_IN_USD
import net.primal.android.premium.legend.PremiumBecomeLegendContract.UiEvent
import net.primal.android.premium.legend.PremiumBecomeLegendContract.UiState
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
        fetchLegendPaymentInstructions()
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
                        )
                    }
                } catch (error: WssException) {
                    Timber.e(error)
                }
            }
        }
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

                    UiEvent.ShowSuccess -> setState {
                        copy(stage = PremiumBecomeLegendContract.BecomeLegendStage.Success)
                    }

                    is UiEvent.UpdateSelectedAmount -> {
                        val newAmountInBtc = it.newAmount.toBigDecimal().setScale(8, RoundingMode.HALF_UP)
                        setState {
                            copy(
                                selectedAmountInBtc = newAmountInBtc,
                                qrCodeValue = "bitcoin:${this.bitcoinAddress}?amount=$newAmountInBtc",
                            )
                        }
                    }
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
}

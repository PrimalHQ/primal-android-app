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
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.utils.getMaximumUsdAmount
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.premium.legend.contribute.LegendContributeContract.LegendContributeState
import net.primal.android.premium.legend.contribute.LegendContributeContract.PaymentMethod
import net.primal.android.premium.legend.contribute.LegendContributeContract.UiEvent
import net.primal.android.premium.legend.contribute.LegendContributeContract.UiState
import net.primal.android.premium.legend.subscription.PurchaseMonitor
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.api.model.MiningFeeTier
import net.primal.android.wallet.api.model.WithdrawRequestBody
import net.primal.android.wallet.domain.CurrencyMode
import net.primal.android.wallet.domain.SubWallet
import net.primal.android.wallet.domain.not
import net.primal.android.wallet.repository.ExchangeRateHandler
import net.primal.android.wallet.repository.WalletRepository
import net.primal.android.wallet.transactions.send.create.ui.model.MiningFeeUi
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
    private val dispatchers: CoroutineDispatcherProvider,
    private val exchangeRateHandler: ExchangeRateHandler,
    private val premiumRepository: PremiumRepository,
    private val walletRepository: WalletRepository,
    private val purchaseMonitor: PurchaseMonitor,
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
                            isFetchingPaymentInstructions = true,
                            primalWalletPaymentInProgress = false,
                            stage = LegendContributeState.PickAmount,
                            lightningInvoice = null,
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

                    UiEvent.ShowPaymentInstructions -> {
                        setState { copy(stage = LegendContributeState.Payment) }
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

                    UiEvent.ReloadMiningFees -> fetchMiningFees()

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
                    onChain = state.value.paymentMethod == PaymentMethod.OnChainBitcoin,
                    amountUsd = state.value.amountInUsd,
                )

                when (state.value.paymentMethod) {
                    PaymentMethod.OnChainBitcoin -> {
                        setState {
                            copy(
                                bitcoinAddress = response.qrCode.parseBitcoinPaymentInstructions()?.address,
                                membershipQuoteId = response.membershipQuoteId,
                                qrCodeValue = response.qrCode,
                            )
                        }
                    }
                    PaymentMethod.BitcoinLightning -> {
                        setState {
                            copy(
                                lightningInvoice = response.qrCode.parseLightningPaymentInstructions(),
                                membershipQuoteId = response.membershipQuoteId,
                                qrCodeValue = response.qrCode,
                            )
                        }
                    }
                    null -> Unit
                }

                startPurchaseMonitor()
                fetchMiningFees()
            } catch (error: WssException) {
                Timber.e(error)
            } finally {
                setState { copy(isFetchingPaymentInstructions = false) }
            }
        }

    private fun fetchMiningFees() {
        val uiState = _state.value
        val btcAddress = uiState.bitcoinAddress
        val amountInSats = uiState.amountInSats

        if (btcAddress == null || amountInSats.toInt() == 0) return

        viewModelScope.launch {
            val lastTierIndex = uiState.selectedFeeTierIndex
            setState { copy(miningFeeTiers = emptyList(), selectedFeeTierIndex = null, isFetchingMiningFees = true) }

            val activeUserId = activeAccountStore.activeUserId()
            try {
                withContext(dispatchers.io()) {
                    val tiers = walletRepository.fetchMiningFees(
                        userId = activeUserId,
                        onChainAddress = btcAddress,
                        amountInBtc = state.value.qrCodeValue?.parseBitcoinPaymentInstructions()?.amount.toString(),
                    )

                    setState {
                        copy(
                            miningFeeTiers = tiers.map { it.asMiningFeeUi() },
                            selectedFeeTierIndex = when {
                                tiers.isNotEmpty() -> when {
                                    lastTierIndex != null && lastTierIndex < tiers.size -> lastTierIndex
                                    else -> 0
                                }

                                else -> null
                            },
                        )
                    }
                }
            } catch (error: WssException) {
                Timber.w(error)
            } finally {
                setState { copy(isFetchingMiningFees = false) }
            }
        }
    }

    private fun withdrawViaPrimalWallet() =
        viewModelScope.launch {
            try {
                setState { copy(primalWalletPaymentInProgress = true) }

                when (state.value.paymentMethod) {
                    PaymentMethod.OnChainBitcoin -> {
                        val miningFeeTier = state.value.selectedFeeTierIndex?.let {
                            state.value.miningFeeTiers.getOrNull(it)
                        }
                        walletRepository.withdraw(
                            userId = activeAccountStore.activeUserId(),
                            body = WithdrawRequestBody(
                                subWallet = SubWallet.Open,
                                targetBtcAddress = state.value.qrCodeValue?.parseBitcoinPaymentInstructions()?.address,
                                amountBtc = state.value.qrCodeValue?.parseBitcoinPaymentInstructions()?.amount,
                                onChainTier = miningFeeTier?.id,
                            ),
                        )
                    }
                    PaymentMethod.BitcoinLightning -> {
                        walletRepository.withdraw(
                            userId = activeAccountStore.activeUserId(),
                            body = WithdrawRequestBody(
                                subWallet = SubWallet.Open,
                                lnInvoice = state.value.lightningInvoice,
                            ),
                        )
                    }
                    null -> Unit
                }
            } catch (error: WssException) {
                Timber.e(error)
                setState { copy(primalWalletPaymentInProgress = false) }
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
        _state.value.membershipQuoteId?.let {
            purchaseMonitor.startMonitor(
                scope = viewModelScope,
                quoteId = it,
            ) {
                setState { copy(stage = LegendContributeState.Success) }
            }
        }
    }

    private fun stopPurchaseMonitor() {
        purchaseMonitor.stopMonitor(viewModelScope)
    }

    private fun MiningFeeTier.asMiningFeeUi(): MiningFeeUi {
        return MiningFeeUi(
            id = this.id,
            label = this.label,
            confirmationEstimateInMin = this.estimatedDeliveryDurationInMin,
            feeInBtc = this.estimatedFee.amount,
            minAmountInBtc = this.minimumAmount?.amount,
        )
    }
}

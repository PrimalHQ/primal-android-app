package net.primal.android.premium.legend.contribute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import java.util.*
import javax.inject.Inject
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.premium.legend.contribute.LegendContributeContract.LegendContributeState
import net.primal.android.premium.legend.contribute.LegendContributeContract.PaymentMethod
import net.primal.android.premium.legend.contribute.LegendContributeContract.UiEvent
import net.primal.android.premium.legend.contribute.LegendContributeContract.UiState
import net.primal.android.premium.legend.subscription.PurchaseMonitor
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.repository.ExchangeRateHandler
import net.primal.core.utils.CurrencyConversionUtils.formatAsString
import net.primal.core.utils.CurrencyConversionUtils.fromSatsToUsd
import net.primal.core.utils.CurrencyConversionUtils.toSats
import net.primal.core.utils.getMaximumUsdAmount
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.rates.fees.TransactionFeeRepository
import net.primal.domain.utils.parseBitcoinPaymentInstructions
import net.primal.domain.utils.parseLightningPaymentInstructions
import net.primal.domain.utils.parseSatsToUsd
import net.primal.domain.utils.parseUsdToSats
import net.primal.domain.wallet.CurrencyMode
import net.primal.domain.wallet.TxRequest
import net.primal.domain.wallet.WalletRepository
import net.primal.domain.wallet.not

@HiltViewModel
class LegendContributeViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val exchangeRateHandler: ExchangeRateHandler,
    private val premiumRepository: PremiumRepository,
    private val walletRepository: WalletRepository,
    private val walletAccountRepository: WalletAccountRepository,
    private val transactionFeeRepository: TransactionFeeRepository,
    private val purchaseMonitor: PurchaseMonitor,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())

    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observeActiveWallet()
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

                    UiEvent.DismissError -> setState { copy(error = null) }

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

    private fun observeActiveWallet() =
        viewModelScope.launch {
            walletAccountRepository.observeActiveWallet(userId = activeAccountStore.activeUserId())
                .collect { wallet ->
                    setState { copy(activeWallet = wallet) }
                }
        }

    private fun observeUsdExchangeRate() =
        viewModelScope.launch {
            exchangeRateHandler.usdExchangeRate.collect {
                setState {
                    copy(
                        currentExchangeRate = it,
                        maximumUsdAmount = getMaximumUsdAmount(it),
                        amountInUsd = _state.value.amountInSats
                            .toBigDecimal()
                            .fromSatsToUsd(it)
                            .toPlainString(),
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
            } catch (error: SignatureException) {
                Napier.e(throwable = error) { "Failed to fetch legend payment instructions due to signature error." }
            } catch (error: NetworkException) {
                Napier.e(throwable = error) { "Failed to fetch legend payment instructions due to network error." }
            } finally {
                setState { copy(isFetchingPaymentInstructions = false) }
            }
        }

    private fun withdrawViaPrimalWallet() =
        viewModelScope.launch {
            runCatching {
                setState { copy(primalWalletPaymentInProgress = true) }
                when (state.value.paymentMethod) {
                    PaymentMethod.OnChainBitcoin -> executeOnChainPayment()
                    PaymentMethod.BitcoinLightning -> executeLightningPayment()
                    null -> Unit
                }
            }.onFailure { error ->
                Napier.w(error) { "Failed to execute legends payment." }
                setState {
                    copy(
                        error = UiState.ContributionUiError.WithdrawViaPrimalWalletFailed(error),
                        primalWalletPaymentInProgress = false,
                    )
                }
            }
        }

    private suspend fun executeOnChainPayment() {
        val activeUserId = activeAccountStore.activeUserId()
        val activeWalletId = state.value.activeWallet?.walletId ?: return
        val instructions = state.value.qrCodeValue?.parseBitcoinPaymentInstructions()
        val targetBtcAddress = instructions?.address
        val amountBtc = instructions?.amount

        if (targetBtcAddress != null && amountBtc != null) {
            val defaultMiningFee = transactionFeeRepository.fetchDefaultMiningFee(
                userId = activeUserId,
                walletId = activeWalletId,
                onChainAddress = targetBtcAddress,
                amountInBtc = amountBtc,
            ).getOrNull()

            walletRepository.pay(
                walletId = activeWalletId,
                request = TxRequest.BitcoinOnChain(
                    amountSats = amountBtc.toSats().toDouble().formatAsString(),
                    noteRecipient = null,
                    noteSelf = null,
                    idempotencyKey = Uuid.random().toString(),
                    onChainAddress = targetBtcAddress,
                    onChainTierId = defaultMiningFee?.tierId,
                ),
            ).getOrThrow()
        }
    }

    private suspend fun executeLightningPayment() {
        val lnInvoice = state.value.lightningInvoice ?: return
        val activeWalletId = state.value.activeWallet?.walletId ?: return
        val amountSats = state.value.amountInSats

        walletRepository.pay(
            walletId = activeWalletId,
            request = TxRequest.Lightning.LnInvoice(
                noteRecipient = null,
                noteSelf = null,
                idempotencyKey = UUID.randomUUID().toString(),
                lnInvoice = lnInvoice,
                amountSats = amountSats,
            ),
        ).getOrThrow()
    }

    private fun updateAmount(amount: String) =
        when (_state.value.currencyMode) {
            CurrencyMode.SATS -> {
                setState {
                    copy(
                        amountInSats = amount,
                        amountInUsd = amount.parseSatsToUsd(state.value.currentExchangeRate),
                        isDonationAmountValid = amount.validateDonationAmount(),
                    )
                }
            }

            CurrencyMode.FIAT -> {
                setState {
                    copy(
                        amountInSats = amount.parseUsdToSats(state.value.currentExchangeRate),
                        amountInUsd = amount,
                        isDonationAmountValid = amount.parseUsdToSats(state.value.currentExchangeRate)
                            .validateDonationAmount(),
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

    private fun String.validateDonationAmount(): Boolean =
        (this.toIntOrNull() ?: 0) >= LegendContributeContract.MIN_DONATION_AMOUNT
}

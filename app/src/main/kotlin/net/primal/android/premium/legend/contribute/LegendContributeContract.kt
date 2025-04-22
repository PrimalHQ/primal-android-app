package net.primal.android.premium.legend.contribute

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import net.primal.android.wallet.domain.CurrencyMode

interface LegendContributeContract {

    companion object {
        const val MIN_DONATION_AMOUNT = 10_000
    }

    data class UiState(
        val stage: LegendContributeState = LegendContributeState.Intro,
        val paymentMethod: PaymentMethod? = null,
        val amountInUsd: String = "0",
        val amountInSats: String = "0",
        val currencyMode: CurrencyMode = CurrencyMode.SATS,
        val maximumUsdAmount: BigDecimal? = null,
        val currentExchangeRate: Double? = null,
        val bitcoinAddress: String? = null,
        val lightningInvoice: String? = null,
        val isFetchingPaymentInstructions: Boolean = true,
        val primalWalletPaymentInProgress: Boolean = false,
        val qrCodeValue: String? = null,
        val membershipQuoteId: String? = null,
        val error: ContributionUiError? = null,
        val isDonationAmountValid: Boolean = false,
    ) {
        fun arePaymentInstructionsAvailable() =
            (this.bitcoinAddress != null || this.lightningInvoice != null) &&
                this.membershipQuoteId != null

        sealed class ContributionUiError {
            data class WithdrawViaPrimalWalletFailed(val cause: Throwable) : ContributionUiError()
        }
    }

    sealed class UiEvent {
        data object GoBackToIntro : UiEvent()
        data object ChangeCurrencyMode : UiEvent()
        data object GoBackToPickAmount : UiEvent()
        data object GoBackToPaymentInstructions : UiEvent()
        data object ShowPaymentInstructions : UiEvent()
        data object ShowSuccess : UiEvent()
        data object StartPurchaseMonitor : UiEvent()
        data object StopPurchaseMonitor : UiEvent()
        data object FetchPaymentInstructions : UiEvent()
        data object PrimalWalletPayment : UiEvent()
        data class ShowAmountEditor(val paymentMethod: PaymentMethod) : UiEvent()
        data class AmountChanged(val amount: String) : UiEvent()
        data object DismissError : UiEvent()
    }

    enum class LegendContributeState {
        Intro,
        PickAmount,
        Payment,
        Success,
    }

    enum class PaymentMethod {
        OnChainBitcoin,
        BitcoinLightning,
    }
}

package net.primal.android.wallet.transactions.receive

import java.math.BigDecimal
import net.primal.android.wallet.domain.CurrencyMode
import net.primal.android.wallet.domain.Network
import net.primal.android.wallet.transactions.receive.model.NetworkDetails
import net.primal.android.wallet.transactions.receive.model.PaymentDetails
import net.primal.android.wallet.transactions.receive.tabs.ReceivePaymentTab

interface ReceivePaymentContract {
    data class UiState(
        val initialTab: ReceivePaymentTab,
        val currentTab: ReceivePaymentTab = initialTab,
        val loading: Boolean = true,
        val editMode: Boolean = false,
        val creatingInvoice: Boolean = false,
        val hasPremium: Boolean = false,
        val lightningNetworkDetails: NetworkDetails = NetworkDetails(network = Network.Lightning),
        val bitcoinNetworkDetails: NetworkDetails = NetworkDetails(network = Network.Bitcoin),
        val paymentDetails: PaymentDetails = PaymentDetails(),
        val currentExchangeRate: Double? = null,
        val maximumUsdAmount: BigDecimal? = null,
        val currencyMode: CurrencyMode = CurrencyMode.SATS,
        val error: ReceivePaymentError? = null,
    ) {
        sealed class ReceivePaymentError {
            data class FailedToCreateLightningInvoice(val cause: Exception) : ReceivePaymentError()
        }
    }

    sealed class UiEvent {
        data object OpenInvoiceCreation : UiEvent()
        data object CancelInvoiceCreation : UiEvent()
        data class CreateInvoice(val amountInBtc: String, val amountInUsd: String, val comment: String?) : UiEvent()
        data class ChangeNetwork(val network: Network) : UiEvent()
        data object DismissError : UiEvent()
    }
}

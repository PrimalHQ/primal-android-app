package net.primal.android.wallet.receive

interface ReceivePaymentContract {
    data class UiState(
        val loading: Boolean = true,
        val editMode: Boolean = false,
        val creating: Boolean = false,
        val paymentDetails: PaymentDetails = PaymentDetails(),
    )

    sealed class UiEvent {
        data object OpenInvoiceCreation : UiEvent()
        data object CancelInvoiceCreation : UiEvent()
        data class CreateInvoice(val amountInBtc: String, val comment: String?) : UiEvent()
    }
}

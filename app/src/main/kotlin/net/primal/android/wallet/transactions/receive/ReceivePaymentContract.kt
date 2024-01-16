package net.primal.android.wallet.transactions.receive

interface ReceivePaymentContract {
    data class UiState(
        val loading: Boolean = true,
        val editMode: Boolean = false,
        val creating: Boolean = false,
        val paymentDetails: PaymentDetails = PaymentDetails(),
        val error: ReceivePaymentError? = null,
    ) {
        sealed class ReceivePaymentError {
            data class FailedToCreateInvoice(val cause: Exception) : ReceivePaymentError()
        }
    }

    sealed class UiEvent {
        data object OpenInvoiceCreation : UiEvent()
        data object CancelInvoiceCreation : UiEvent()
        data class CreateInvoice(val amountInBtc: String, val comment: String?) : UiEvent()
        data object DismissError : UiEvent()
    }
}

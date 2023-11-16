package net.primal.android.wallet.model

import kotlinx.serialization.Serializable

@Serializable
data class LightningPayResponse(
    val pr: String,
) {
    fun toWalletPayRequest(): WalletRequest<PayInvoiceRequest> =
        WalletRequest(
            method = "pay_invoice",
            params = PayInvoiceRequest(this.pr),
        )
}

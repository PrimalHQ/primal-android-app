package net.primal.android.wallet.nwc.model

import kotlinx.serialization.Serializable

@Serializable
data class LightningPayResponse(
    val pr: String,
) {
    fun toWalletPayRequest(): NwcWalletRequest<PayInvoiceRequest> =
        NwcWalletRequest(
            method = "pay_invoice",
            params = PayInvoiceRequest(this.pr),
        )
}

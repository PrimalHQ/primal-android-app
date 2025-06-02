package net.primal.core.networking.nwc.model

import kotlinx.serialization.Serializable
import net.primal.core.networking.nwc.nip47.PayInvoiceParams

@Serializable
data class LightningPayResponse(
    val pr: String,
) {
    fun toNwcPayInvoiceParams(): PayInvoiceParams =
        PayInvoiceParams(invoice = this.pr)

    fun toWalletPayRequest(): NwcWalletRequest<PayInvoiceParams> =
        NwcWalletRequest(
            method = "pay_invoice",
            params = toNwcPayInvoiceParams(),
        )
}

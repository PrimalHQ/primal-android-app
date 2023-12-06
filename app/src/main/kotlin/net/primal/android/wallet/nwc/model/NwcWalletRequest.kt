package net.primal.android.wallet.nwc.model

import kotlinx.serialization.Serializable

@Serializable
data class NwcWalletRequest<T>(
    val method: String,
    val params: T,
)

@Serializable
data class PayInvoiceRequest(
    val invoice: String,
)

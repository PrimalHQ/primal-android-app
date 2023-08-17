package net.primal.android.nostr.model.zap

import kotlinx.serialization.Serializable

@Serializable
data class WalletRequest<T>(val method: String, val params: T)

@Serializable
data class PayInvoiceRequest(val invoice: String)

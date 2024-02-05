package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParsedLnInvoiceResponse(
    @SerialName("lninvoice") var lnInvoiceData: LnInvoiceData,
    @SerialName("pubkey") var userId: String? = null,
    val comment: String? = null,
)

@Serializable
data class LnInvoiceData(
    @SerialName("amount_msat") var amountMilliSats: Int,
    var description: String? = null,
    val signature: String? = null,
    val currency: String? = null,
    @SerialName("payment_secret") val paymentSecret: String? = null,
    val date: Long? = null,
    val expiry: Long? = null,
    val payee: String? = null,
    @SerialName("payment_hash") val paymentHash: String? = null,
)

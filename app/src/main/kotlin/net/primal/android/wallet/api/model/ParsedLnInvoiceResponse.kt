package net.primal.android.wallet.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParsedLnInvoiceResponse(
    @SerialName("lninvoice") var lnInvoiceData: LnInvoiceData,
    @SerialName("pubkey") var userId: String?,
    val comment: String?,
)

@Serializable
data class LnInvoiceData(
    @SerialName("amount_msat") var amountMilliSats: Int,
    var description: String?,
    val signature: String?,
    val currency: String?,
    @SerialName("payment_secret") val paymentSecret: String?,
    val date: Long?,
    val expiry: Long?,
    val payee: String?,
    @SerialName("payment_hash") val paymentHash: String?,
)

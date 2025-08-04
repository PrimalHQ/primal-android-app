package net.primal.wallet.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InAppPurchaseQuoteResponse(
    @SerialName("quote_id") val quoteId: String,
    @SerialName("amount_btc") val amountBtc: String,
)
